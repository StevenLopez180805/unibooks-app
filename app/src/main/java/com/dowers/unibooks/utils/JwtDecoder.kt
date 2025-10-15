package com.dowers.unibooks.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT

data class UserInfo(
    val name: String,
    val role: String,
    val email: String
)

object JwtDecoder {
    
    fun decodeToken(token: String): UserInfo? {
        return try {
            val decodedJWT: DecodedJWT = JWT.decode(token)
            
            val name = decodedJWT.getClaim("name").asString() ?: "Usuario"
            val role = decodedJWT.getClaim("role").asString() ?: "estudiante"
            val email = decodedJWT.getClaim("email").asString() ?: ""
            
            UserInfo(name = name, role = role, email = email)
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
