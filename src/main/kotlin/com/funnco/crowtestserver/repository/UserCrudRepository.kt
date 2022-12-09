package com.funnco.crowtestserver.repository

import com.funnco.crowtestserver.db_entity.UserEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface UserCrudRepository : CrudRepository<UserEntity, UUID> {
    fun findByMail(email: String): UserEntity?
    fun findByToken(token: String): UserEntity?
}