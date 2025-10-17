package com.dowers.unibooks.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT

data class UserInfo(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val name: String,
    val role: String,
    val email: String
)

object JwtDecoder {
    
    fun decodeToken(token: String): UserInfo? {
        return try {
            val decodedJWT: DecodedJWT = JWT.decode(token)
            val id = decodedJWT.getClaim("sub").asInt() ?: 0
            val firstName = decodedJWT.getClaim("firstName").asString() ?: "Usuario"
            val lastName = decodedJWT.getClaim("lastName").asString() ?: "Usuario"
            val name = "$firstName $lastName"
            val role = decodedJWT.getClaim("role").asString() ?: "estudiante"
            val email = decodedJWT.getClaim("email").asString() ?: ""
            
            UserInfo(id = id, firstName = firstName, lastName = lastName, name = name, role = role, email = email)
        } catch (e: Exception) {
            android.util.Log.e("JwtDecoder", "Error decodificando JWT: ${e.message}")
            null
        }
    }
    
    fun isLibrarian(token: String): Boolean {
        val userInfo = decodeToken(token)
        return userInfo?.role == "bibliotecario"
    }
}
