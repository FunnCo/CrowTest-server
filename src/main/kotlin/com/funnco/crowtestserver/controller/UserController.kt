package com.funnco.crowtestserver.controller

import com.funnco.crowtestserver.db_entity.UserEntity
import com.funnco.crowtestserver.model.UserInfo
import com.funnco.crowtestserver.repository.UserCrudRepository
import com.funnco.crowtestserver.utils.HashingUtil
import com.funnco.crowtestserver.utils.RestControllerUtil
import com.funnco.crowtestserver.utils.UserUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class UserController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var userRepository: UserCrudRepository

    @GetMapping("/user/login")
    fun authViaPassword(@RequestParam mail: String, password: String) : Any?{
        val logtag = "/user/login:  "

        logger.info("$logtag Received login request with email $mail")

        var userPassword = password

        // Checking if user actually exists
        val currentUser = userRepository.findByMail(mail)
        if(currentUser == null) {
            logger.error("$logtag No user with such mail found: $mail")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.NOT_FOUND, "Incorrect email or password")
        }

        // Password validation
        userPassword = HashingUtil.hashPassword(password, currentUser!!.userId!!)
        if(userPassword != currentUser.password){
            logger.error("$logtag Password is incorrect: $mail")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.NOT_FOUND, "Incorrect email or password")
        }

        // Regenerating token, if it didn't exist
            currentUser.token =  HashingUtil.md5Hash("${Random().nextLong()}Salt${currentUser.mail}${currentUser.name}")
            logger.info("$logtag Created new token: ${currentUser.token}")
            userRepository.save(currentUser)

        logger.info("$logtag Returning token: ${currentUser.token}")
        return object {
            val token = currentUser.token!!.toString()
        }
    }

    @GetMapping("/user/login/token")
    fun authViaToken(@RequestHeader("Authorization") token: String): UserInfo {
        val logtag = "/user/login/token:  "

        logger.info("$logtag Received login request with token: $token")

        val currentUser = userRepository.findByToken(token)
        if(currentUser == null){
            logger.error("$logtag No user with such token found: $token")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.NOT_FOUND, "Can't find user with passed token")
        }
        return UserInfo(currentUser!!.name!!, currentUser.grade!!, currentUser.mail!!)
    }

    @PostMapping("/user/register")
    fun register(@RequestBody newUser: UserEntity){
        val logtag = "/user/register:  "

        logger.info("$logtag Received register request with email ${newUser.mail}")

        // Check if user is already exists
        var currentUser = userRepository.findByMail(newUser.mail!!)
        if(currentUser != null){
            logger.error("$logtag User with such email already exists ${newUser.mail}")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.UNAUTHORIZED, "User with same mail or phone already exists")
        }

        // Checking if all necessary fields are not empty
        if(newUser.password == null || newUser.mail == null || newUser.name == null || newUser.grade == null) {
            logger.error("$logtag Some of required fields are empty ${newUser.mail}")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.BAD_REQUEST, "Not all necessary fields are passed")
        }

        // Phone validation
        if(UserUtil.isMailValid(newUser.mail!!)){
            logger.error("$logtag Email is incorrect ${newUser.mail}")
            RestControllerUtil.throwException(RestControllerUtil.HTTPResponseStatus.BAD_REQUEST, "Invalid mail")
        }

        // Creating new user
        currentUser = UserEntity()
        currentUser.userId = UUID.randomUUID()

        // Hashing and salting for saving in DB
        currentUser.password = HashingUtil.hashPassword(newUser.password!!, currentUser.userId!!)

        // Setting rest of the
        currentUser.name = newUser.name
        currentUser.token = null
        currentUser.mail = newUser.mail
        currentUser.grade = newUser.grade

        // Saving user in DB
        userRepository.save(currentUser)
        logger.info("$logtag New user saved ${newUser.mail}")
    }

}