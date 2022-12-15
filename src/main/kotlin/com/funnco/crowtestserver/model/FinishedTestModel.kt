package com.funnco.crowtestserver.model

import com.funnco.crowtestserver.db_entity.UserTestEntity
import java.time.format.DateTimeFormatter

class FinishedTestModel(
    val id: String?,
    val heading: String?,
    val description: String?,
    val deadLineDate: String?,
    val startDate: String?,
    val timeForSolving: Int?,
    val mark: Int?,
    val solveDate: String?,
    val solvingTime: Double?,
) {

    companion object{
        fun parseFromEntity(entity: UserTestEntity, ): FinishedTestModel {
            return FinishedTestModel(
                id = entity.testId.toString(),
                heading = entity.refTestEntity!!.heading,
                description = entity.refTestEntity!!.descripiton,
                deadLineDate = entity.refTestEntity!!.deadlineDate!!.toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                startDate = entity.refTestEntity!!.startDate!!.toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                solvingTime = entity.timeUsed!!,
                mark = entity.mark,
                timeForSolving = entity.refTestEntity!!.timeForSolving,
                solveDate = entity.solveDate!!.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            )
        }
    }
}