package dev.jx.ec04.entity

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val firstname: String? = null,
    val lastname: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
)
