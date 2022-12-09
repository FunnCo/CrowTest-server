package com.funnco.crowtestserver.db_entity

import com.fasterxml.jackson.databind.JsonNode
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "user_test", schema = "application", catalog = "CrowTest")
@TypeDefs(
    TypeDef(name = "json", typeClass = JsonType::class),
    TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
)
@IdClass(UserTestEntityPK::class)
open class UserTestEntity {
    @Id
    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    open var userId: UUID? = null

    @Id
    @Column(name = "test_id", nullable = false, insertable = false, updatable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    open var testId: UUID? = null

    @Basic
    @Column(name = "solve_date", nullable = false)
    open var solveDate: java.sql.Date? = null

    @Basic
    @Column(name = "mark", nullable = true)
    open var mark: Int? = null

    @Basic
    @Column(name = "time_used", nullable = true)
    open var timeUsed: Double? = null

    @Basic
    @Type(type = "json")
    @Column(name = "answers", nullable = true, columnDefinition = "json")
    open var answers: JsonNode? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false, insertable = false)
    open var refUserEntity: UserEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", referencedColumnName = "test_id", updatable = false, insertable = false)
    open var refTestEntity: TestEntity? = null

    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "userId = $userId " +
                "testId = $testId " +
                "solveDate = $solveDate " +
                "mark = $mark " +
                "timeUsed = $timeUsed " +
                "answers = $answers " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserTestEntity

        if (userId != other.userId) return false
        if (testId != other.testId) return false
        if (solveDate != other.solveDate) return false
        if (mark != other.mark) return false
        if (timeUsed != other.timeUsed) return false
        if (answers != other.answers) return false

        return true
    }

}

class UserTestEntityPK : java.io.Serializable {
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    var userId: UUID? = null

    @Id
    @Column(name = "test_id", nullable = false, updatable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    var testId: UUID? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserTestEntityPK

        if (userId != other.userId) return false
        if (testId != other.testId) return false

        return true
    }

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

}
