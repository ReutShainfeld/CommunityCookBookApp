package com.cookbook.app.model

import java.io.Serializable

data class User(
    var userId: String = "",
    var name: String = "",
    val email: String = "",
    var profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable{
    constructor():this("","","","",0,0)
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
