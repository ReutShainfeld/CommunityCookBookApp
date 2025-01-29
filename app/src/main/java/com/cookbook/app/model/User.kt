package com.cookbook.app.model

import java.io.Serializable

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable
