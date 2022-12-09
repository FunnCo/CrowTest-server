package com.funnco.crowtestserver.model

class NewTestModel(
    val heading: String?,
    val description: String?,
    val deadLineDate: String?,
    val startDate: String?,
    val timeForSolving: Int?,
    val questions: Map<String, Any>?
)