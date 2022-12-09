package com.funnco.crowtestserver.repository

import com.funnco.crowtestserver.db_entity.TestEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TestCrudRepository : CrudRepository<TestEntity, UUID> {

}