package com.cookbook.app.utils

object ValidationUtil {

    fun isEmpty(value: String): Boolean {
        return value.trim().isEmpty()
    }

    fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return email.matches(emailRegex)
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

}