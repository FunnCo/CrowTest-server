package com.funnco.crowtestserver.utils

object UserUtil {
    fun isMailValid (inputPhone: String): Boolean{
        return !inputPhone.matches("^[a-z1-9.\\-_]+\\@[a-z1-9]+\\.[a-z]{2,3}\$".toRegex())
    }
}