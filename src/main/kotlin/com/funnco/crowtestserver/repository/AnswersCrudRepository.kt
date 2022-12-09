package com.funnco.crowtestserver.repository

import com.funnco.crowtestserver.db_entity.TestEntity
import com.funnco.crowtestserver.db_entity.UserTestEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface AnswersCrudRepository : CrudRepository<UserTestEntity, UUID> {
    fun findAllByUserId(uuid: UUID): List<UserTestEntity>?
}