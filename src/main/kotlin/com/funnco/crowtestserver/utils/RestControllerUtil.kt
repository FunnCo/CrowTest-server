package com.funnco.crowtestserver.utils

import com.funnco.crowtestserver.db_entity.UserEntity
import com.funnco.crowtestserver.repository.UserCrudRepository
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

object RestControllerUtil {

    enum class HTTPResponseStatus(val code: HttpStatus){
        BAD_REQUEST(HttpStatus.BAD_REQUEST),
        NOT_FOUND(HttpStatus.NOT_FOUND),
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
        OK(HttpStatus.OK),
        CREATED(HttpStatus.CREATED)
    }

    fun throwException(status: HTTPResponseStatus, message: String) {
        throw ResponseStatusException(
            status.code, message
        )
    }

    fun getUserByToken(userRepository: UserCrudRepository, token: String): UserEntity {
        val currentUser = userRepository.findByToken(token)
        if (currentUser == null) {
            RestControllerUtil.throwException(HTTPResponseStatus.UNAUTHORIZED, "Invalid token")
        }
        return currentUser!!
    }
}