package com.funnco.crowtestserver.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.funnco.crowtestserver.db_entity.TestEntity
import com.funnco.crowtestserver.model.NewTestModel
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
import java.util.Collections
import java.util.UUID
import kotlin.math.log

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
    fun addTest(@RequestHeader("Authorization") token: String, @RequestBody newTest: NewTestModel) {
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
            newTest.questions == null
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
        testCrudRepository.save(entity)
    }

    @GetMapping("/test/get/available")
    fun getAvailableTests(@RequestHeader("Authorization") token: String): List<TestEntity> {
        val logTag = "/test/get/available:  "
        val currentUser = RestControllerUtil.getUserByToken(userRepository, token)
        logger.info("$logTag Received request for getting all available requests for ${currentUser.mail}")

        val listOfAllTests = testCrudRepository.findAll()
        val listOfDoneTests = answersCrudRepository.findAllByUserId(currentUser.userId!!)

        var listOfAvailableTests : List<TestEntity> = mutableListOf<TestEntity>()

        // Searching for available tests
        if(listOfDoneTests!=null) {
            listOfAllTests.forEach { testEntity ->
                var isFinished = false
                for (finishedTest in listOfDoneTests) {
                    if(finishedTest.testId == testEntity.testId){
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
        for(test in listOfAvailableTests){
            test.questions = null
        }

        // returning test
        // TODO: Change return type to ResponseAvailableTest
        return listOfAvailableTests
    }

    @GetMapping("/test/get/questions")
    fun getQuestionsForTest(@RequestHeader("Authorization") token: String, @RequestParam("testId") testId: String): JsonNode{
        val logTag = "/test/get/questions:  "
        val currentUser = RestControllerUtil.getUserByToken(userRepository, token)
        logger.info("$logTag Received request for getting questions by ${currentUser.mail} for test $testId")

        val searchedTest = testCrudRepository.findByIdOrNull(UUID.fromString(testId))
        if(searchedTest == null){
            logger.error("$logTag test with id $testId is not found")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.BAD_REQUEST, "Test with passed id is not found")
        }

        // TODO: Need to unselect all answered and shuffle answers
        val questions = searchedTest!!.questions!!.get("list") as  ArrayNode
        val responseJson = ObjectMapper().nodeFactory.arrayNode()
        for(item in questions){
            val answers = item.get("answers")
            if(answers != null){

            } else {
                (item as ObjectNode).put("answer", "")
            }
        }

        return questions
    }
}