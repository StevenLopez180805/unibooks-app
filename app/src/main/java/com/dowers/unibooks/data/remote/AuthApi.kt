package com.dowers.unibooks.data.remote
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import com.dowers.unibooks.data.models.Book

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String
)

data class CreateBookRequest(
    val titulo: String,
    val descripcion: String,
    val escritor: String,
    val ubicacion: String,
    val stock: Int
)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @GET("libros")
    suspend fun getBooks(
        @Header("Authorization") token: String
    ): Response<List<Book>>
    
    @POST("libros")
    suspend fun createBook(
        @Header("Authorization") token: String,
        @Body request: CreateBookRequest
    ): Response<Book>
}