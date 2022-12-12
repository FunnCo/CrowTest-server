package com.funnco.crowtestserver.db_entity


import com.fasterxml.jackson.databind.JsonNode
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonType
import javax.persistence.*
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.util.*

@Entity
@Table(name = "test", schema = "application", catalog = "CrowTest")
@TypeDefs(
    TypeDef(name = "json", typeClass = JsonType::class),
    TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
)
open class TestEntity {
    @Id
    @Column(name = "test_id", insertable=false, updatable=false, nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    open var testId: UUID? = null

    @Basic
    @Column(name = "heading", nullable = false)
    open var heading: String? = null

    @Basic
    @Column(name = "descripiton", nullable = true)
    open var descripiton: String? = null

    @Basic
    @Column(name = "deadline_date", nullable = false)
    open var deadlineDate: java.sql.Date? = null

    @Basic
    @Column(name = "start_date", nullable = false)
    open var startDate: java.sql.Date? = null

    @Basic
    @Column(name = "time_for_solving", nullable = true)
    open var timeForSolving: Int? = null

    @Basic
    @Column(name = "criteria_excellent", nullable = true)
    open var criteriaExcellent: Int? = null

    @Basic
    @Column(name = "criteria_good", nullable = true)
    open var criteriaGood : Int? = null

    @Basic
    @Column(name = "criteria_pass", nullable = true)
    open var criteriaPass: Int? = null


    @Type(type = "json")
    @Column(name = "questions", nullable = false, columnDefinition = "json")
    open var questions: JsonNode? = null

    @OneToMany(mappedBy = "refTestEntity", targetEntity=UserTestEntity::class)
    open var refUserTestEntities: List<UserTestEntity>? = null

    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "testId = $testId " +
                "heading = $heading " +
                "descripiton = $descripiton " +
                "deadlineDate = $deadlineDate " +
                "startDate = $startDate " +
                "timeForSolving = $timeForSolving " +
                "questions = $questions " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TestEntity

        if (testId != other.testId) return false
        if (heading != other.heading) return false
        if (descripiton != other.descripiton) return false
        if (deadlineDate != other.deadlineDate) return false
        if (startDate != other.startDate) return false
        if (timeForSolving != other.timeForSolving) return false
        if (questions != other.questions) return false

        return true
    }

}

