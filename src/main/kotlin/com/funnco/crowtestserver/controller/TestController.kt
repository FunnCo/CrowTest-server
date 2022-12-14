package com.funnco.crowtestserver.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.funnco.crowtestserver.db_entity.TestEntity
import com.funnco.crowtestserver.db_entity.UserTestEntity
import com.funnco.crowtestserver.model.FinishedTestModel
import com.funnco.crowtestserver.model.NewTestModel
import com.funnco.crowtestserver.model.ResponseAvailableTest
import com.funnco.crowtestserver.repository.AnswersCrudRepository
import com.funnco.crowtestserver.repository.TestCrudRepository
import com.funnco.crowtestserver.repository.UserCrudRepository
import com.funnco.crowtestserver.utils.RestControllerUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
class TestController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var answersCrudRepository: AnswersCrudRepository

    @Autowired
    private lateinit var userRepository: UserCrudRepository

    @Autowired
    private lateinit var testCrudRepository: TestCrudRepository

    @PostMapping("/test/add")
    fun addTest(@RequestHeader("Authorization") token: String, @RequestBody newTest: NewTestModel, @RequestParam("grade") grade: String) {
        val logTag = "/test/add:  "
        logger.info("$logTag Received request for adding new test: ${newTest.heading}")

        // Authorizing user
        if (token != "-1") {
            logger.error("$logTag User doesn't have enough privileges for adding new test: ${newTest.heading}")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.UNAUTHORIZED, "Access forbidden")
        }

        // Checking if all fields are not null
        if (newTest.heading == null ||
            newTest.description == null ||
            newTest.deadLineDate == null ||
            newTest.startDate == null ||
            newTest.timeForSolving == null ||
            newTest.questions == null ||
            newTest.criteriaExcellent == null ||
            newTest.criteriaGood == null ||
            newTest.criteriaPass == null
        ) {
            logger.error("$logTag Some of the fields are empty for test: ${newTest.heading}")
            RestControllerUtil.throwException(
                RestControllerUtil.HTTPResponseStatus.BAD_REQUEST,
                "Not all fields are filled"
            )
        }

        // Dealing with dates
        val startDateInJava = LocalDate.parse(newTest.startDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val startDateSQL = java.sql.Date(
            java.util.Date.from(
                startDateInJava.atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant()
            ).time
        )

        val deadlineDateInJava = LocalDate.parse(newTest.deadLineDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val deadlineDateSQL = java.sql.Date(
            java.util.Date.from(
                deadlineDateInJava.atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant()
            ).time
        )

        // Dealing with Questions JSON
        val mapper = ObjectMapper()
        val jsonOfQuestions = mapper.convertValue(newTest.questions, JsonNode::class.java)

        // Adding new test into DB
        val entity = TestEntity()
        entity.heading = newTest.heading
        entity.deadlineDate = deadlineDateSQL
        entity.startDate = startDateSQL
        entity.descripiton = newTest.description
        entity.timeForSolving = newTest.timeForSolving
        entity.questions = jsonOfQuestions
        entity.testId = UUID.randomUUID()
        entity.criteriaPass = newTest.criteriaPass
        entity.criteriaExcellent = newTest.criteriaExcellent
        entity.criteriaGood = newTest.criteriaGood
        entity.grade = grade

        testCrudRepository.save(entity)
    }

    @GetMapping("/test/get/available")
    fun getAvailableTests(@RequestHeader("Authorization") token: String): List<ResponseAvailableTest> {
        val logTag = "/test/get/available:  "
        val currentUser = RestControllerUtil.getUserByToken(userRepository, token)
        logger.info("$logTag Received request for getting all available requests for ${currentUser.mail}")

        val listOfAllTests = testCrudRepository.findAll()
        val listOfDoneTests = answersCrudRepository.findAllByUserId(currentUser.userId!!)

        var listOfAvailableTests: List<TestEntity> = mutableListOf<TestEntity>()

        // Searching for available tests
        if (listOfDoneTests != null) {
            listOfAllTests.forEach { testEntity ->
                var isFinished = false
                for (finishedTest in listOfDoneTests) {
                    if (finishedTest.testId == testEntity.testId) {
                        isFinished = true
                        break
                    }
                }
                if (!isFinished) {
                    (listOfAvailableTests as MutableList).add(testEntity)
                }
            }
        } else {
            listOfAvailableTests = listOfAllTests.toList()
        }

        // deleting questions from every test
        for (test in listOfAvailableTests) {
            test.questions = null
        }

        // parsing TestEntities to ResponseAvailableTests
        val responseList: List<ResponseAvailableTest> = mutableListOf()
        listOfAvailableTests.forEach {
            (responseList as MutableList).add(
                ResponseAvailableTest(
                    testId = it.testId!!.toString(),
                    heading = it.heading!!,
                    description = it.descripiton!!,
                    deadlineDate = it.deadlineDate!!.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    startDate = it.startDate!!.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    timeForSolving = it.timeForSolving!!
                )
            )
        }

        // returning test
        return responseList
    }

    @GetMapping("/test/get/questions")
    fun getQuestionsForTest(
        @RequestHeader("Authorization") token: String,
        @RequestParam("testId") testId: String,
    ): JsonNode {
        val logTag = "/test/get/questions:  "
        val currentUser = RestControllerUtil.getUserByToken(userRepository, token)
        logger.info("$logTag Received request for getting questions by ${currentUser.mail} for test $testId")

        val searchedTest = testCrudRepository.findByIdOrNull(UUID.fromString(testId))
        if (searchedTest == null) {
            logger.error("$logTag test with id $testId is not found")
            RestControllerUtil.throwException(
                RestControllerUtil.HTTPResponseStatus.BAD_REQUEST,
                "Test with passed id is not found"
            )
        }

        val questions = searchedTest!!.questions!!.get("list") as ArrayNode
        val responseJson = ObjectMapper().nodeFactory.arrayNode()
        for (item in questions) {
            val currentQuestion = ObjectMapper().nodeFactory.objectNode()
            currentQuestion.put("task", item.get("task"))
            currentQuestion.put("type", item.get("type"))
            when (item.get("type").asText()) {
                "one_answer", "multiple_answer" -> {
                    val currentAnswers = ObjectMapper().nodeFactory.arrayNode()
                    val startingAnswers = item.get("answers")
                    val listOfAnswers = mutableListOf<JsonNode>()
                    for (answer in startingAnswers) {
                        listOfAnswers.add(answer)
                    }
                    listOfAnswers.shuffle()
                    for (answer in listOfAnswers) {
                        currentAnswers.add(answer)
                    }
                    currentQuestion.put("answers", currentAnswers)
                }
                "accordance_answer" -> {
                    currentQuestion.put("firstListOfAnswers", item.get("firstListOfAnswers"))

                    val currentAnswers = ObjectMapper().nodeFactory.arrayNode()
                    val startingAnswers = item.get("secondListOfAnswers")
                    val listOfAnswers = mutableListOf<JsonNode>()
                    for (answer in startingAnswers) {
                        listOfAnswers.add(answer)
                    }
                    listOfAnswers.shuffle()
                    for (answer in listOfAnswers) {
                        currentAnswers.add(answer)
                    }
                    currentQuestion.put("secondListOfAnswers", currentAnswers)
                }
                "input_answer" -> {
                    val answerNode = ObjectMapper().nodeFactory.objectNode()
                    answerNode.put("content", "")
                    currentQuestion.put("answer", answerNode)
                }
            }
            responseJson.add(currentQuestion)
        }

        return responseJson
    }

    @PostMapping("/test/post/answers")
    fun postAnswersForTests(
        @RequestHeader("Authorization") token: String,
        @RequestParam("testId") testId: String,
        @RequestBody userAnswers: JsonNode,
        @RequestParam("solvingTime") solvingTime: Double
    ) {
        val logTag = "/test/post/answers:  "
        val currentUser = RestControllerUtil.getUserByToken(userRepository, token)
        logger.info("$logTag Received request for posting answers by ${currentUser.mail} for test $testId")

        val searchedTest = testCrudRepository.findByIdOrNull(UUID.fromString(testId))
        if (searchedTest == null) {
            logger.error("$logTag test with id $testId is not found")
            RestControllerUtil.throwException(
                RestControllerUtil.HTTPResponseStatus.BAD_REQUEST,
                "Test with passed id is not found"
            )
        }

        val questionsFromDB = searchedTest!!.questions!!.get("list") as ArrayNode
        val realAnswers = userAnswers as ArrayNode
        var correctQuestions = 0

        // determining count of correct answers
        for (answeredQuestion in realAnswers) {
            val correspondingQuestionFromDb =
                questionsFromDB.find { it.get("task").asText() == answeredQuestion.get("task").asText() }
            if (correspondingQuestionFromDb == null) {
                logger.error("One of the received answers doesn't correspond to any question (info: ${currentUser.mail} for test $testId)")
                RestControllerUtil.throwException(
                    RestControllerUtil.HTTPResponseStatus.BAD_REQUEST,
                    "One of the received answers doesn't correspond to any question"
                )
            }
            when (correspondingQuestionFromDb!!.get("type").asText()) {
                "one_answer", "multiple_answer" -> {
                    val realAnswers = answeredQuestion.get("answers") as ArrayNode
                    val expectedAnswers = correspondingQuestionFromDb.get("answers") as ArrayNode
                    var totalVariants = expectedAnswers.size()
                    var totalCorrectVariants = 0
                    for (expectedAnswer in expectedAnswers) {
                        var isCorrect = false
                        val currentContent = expectedAnswer.get("content").asText()
                        val currentSelection = expectedAnswer.get("isSelected").asBoolean()
                        for (realAnswer in realAnswers) {
                            if (realAnswer.get("content").asText() == currentContent && realAnswer.get("isSelected")
                                    .asBoolean() == currentSelection
                            ) {
                                isCorrect = true
                                break
                            }
                        }
                        if (isCorrect) {
                            totalCorrectVariants++
                        }
                    }
                    if (totalVariants == totalCorrectVariants) {
                        correctQuestions++
                    }
                }
                "accordance_answer" -> {
                    val realAnswers = parseAccordanceToMap(
                        answeredQuestion.get("firstListOfAnswers") as ArrayNode,
                        answeredQuestion.get("secondListOfAnswers") as ArrayNode
                    )

                    val expectedAnswers = parseAccordanceToMap(
                        correspondingQuestionFromDb.get("firstListOfAnswers") as ArrayNode,
                        correspondingQuestionFromDb.get("secondListOfAnswers") as ArrayNode
                    )

                    if (realAnswers.equals(expectedAnswers)) {
                        correctQuestions++
                    }
                }
                "input_answer" -> {
                    val realAnswer = answeredQuestion.get("answer").get("content").asText()
                    val expectedAnswer = correspondingQuestionFromDb.get("answer").get("content").asText()
                    if (realAnswer.lowercase() == expectedAnswer.lowercase()) {
                        correctQuestions++
                    }
                }
            }
        }

        // Determining mark
        var mark = 2
        if (correctQuestions >= searchedTest.criteriaExcellent!!) {
            mark = 5
        } else if (correctQuestions >= searchedTest.criteriaGood!!) {
            mark = 4
        } else if (correctQuestions >= searchedTest.criteriaPass!!) {
            mark = 3
        }

        val solvingDate = LocalDate.now()
        val solvingDateSQL = java.sql.Date(
            java.util.Date.from(
                solvingDate.atStartOfDay(ZoneId.of("Europe/Moscow")).toInstant()
            ).time
        )

        val resultEntity = UserTestEntity()
        resultEntity.testId = searchedTest.testId
        resultEntity.userId = currentUser.userId
        resultEntity.answers = userAnswers
        resultEntity.mark = mark
        resultEntity.solveDate = solvingDateSQL
        resultEntity.timeUsed = solvingTime

        logger.info("User with mail ${currentUser.mail} finished test ${searchedTest.testId} with mark $mark")

        answersCrudRepository.save(resultEntity)
    }

    @GetMapping("/test/get/finished")
    fun getFinishedTests(
        @RequestHeader("Authorization") token: String,
    ): List<FinishedTestModel> {
        val logTag = "/test/get/finished:  "
        val currentUser = RestControllerUtil.getUserByToken(userRepository, token)
        logger.info("$logTag Received request for getting finished tests from ${currentUser.mail}")

        val resultList = mutableListOf<FinishedTestModel>()
        answersCrudRepository.findAllByUserId(currentUser.userId!!)
            ?.forEach { resultList.add(FinishedTestModel.parseFromEntity(it)) }
        return resultList
    }

    @GetMapping("/test/get/statistics")
    fun getGradeStatistics(@RequestHeader("Authorization") token: String, @RequestParam("grade") grade: String, @RequestParam("testId") testId: String){
        val logTag = "/test/get/statistics:  "
        logger.info("$logTag Received request for getting statisitcs: $testId")

        // Authorizing user
        if (token != "-1") {
            logger.error("$logTag User doesn't have enough privileges for adding new test: $testId")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.UNAUTHORIZED, "Access forbidden")
        }

        // Searching for test
        val searchedTest = testCrudRepository.findByIdOrNull(UUID.fromString(testId))
        if (searchedTest == null) {
            logger.error("$logTag test with id $testId is not found")
            RestControllerUtil.throwException(
                RestControllerUtil.HTTPResponseStatus.BAD_REQUEST,
                "Test with passed id is not found"
            )
        }
    }

    private fun parseAccordanceToMap(questions: ArrayNode, answers: ArrayNode): Map<String, String> {
        val resultMap = mutableMapOf<String, String>()
        for (i in 0 until questions.size()) {
            resultMap[questions[i].get("content").asText()] = answers[i].get("content").asText()
        }
        return resultMap
    }
}