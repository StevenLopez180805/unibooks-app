package com.dowers.unibooks.data.models

data class User(
    val id: Int,
    val firstName: String,
    val secondName: String,
    val lastName: String,
    val secondLastName: String,
    val cedula: String,
    val email: String,
    val password: String,
    val role: String = "estudiante"
)
