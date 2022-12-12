package com.funnco.crowtestserver.model

open class NewTestModel(
    val heading: String?,
    val description: String?,
    val deadLineDate: String?,
    val startDate: String?,
    val timeForSolving: Int?,
    val questions: Map<String, Any>?,
    val criteriaExcellent: Int?,
    val criteriaGood: Int?,
    val criteriaPass: Int?
)