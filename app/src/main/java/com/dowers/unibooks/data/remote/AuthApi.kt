package com.dowers.unibooks.data.remote
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import com.dowers.unibooks.data.models.Book
import com.dowers.unibooks.data.models.User
import retrofit2.http.Query

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

data class CreateUserRequest(
    val firstName: String,
    val secondName: String,
    val lastName: String,
    val secondLastName: String,
    val cedula: String,
    val email: String,
    val password: String,
    val role: String = "estudiante"
)

data class CreatePrestamoRequest(
    val fechaPrestamo: String,
    val fechaDevolucionEsperada: String,
    val fechaDevolucion: String? = null,
    val user: UserRef,
    val libro: List<LibroRef>
)

data class UserRef(
    val id: Int
)

data class LibroRef(
    val id: Int
)

data class PrestamoResponse(
    val id: Int,
    val fechaPrestamo: String,
    val fechaDevolucionEsperada: String,
    val fechaDevolucion: String?,
    val user: User,
    val libro: List<Book>
)

data class ErrorResponse(
    val statusCode: Int,
    val message: String,
    val error: String,
    val code: String
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
    
    @PATCH("libros/{id}")
    suspend fun updateBook(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateBookRequest
    ): Response<Book>
    
    @DELETE("libros/{id}")
    suspend fun deleteBook(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    // Endpoints para usuarios
    @GET("users")
    suspend fun getUsers(
        @Header("Authorization") token: String
    ): Response<List<User>>
    
    @POST("users")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): Response<User>
    
    @PATCH("users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateUserRequest
    ): Response<User>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    // Endpoints para préstamos
    @GET("prestamos")
    suspend fun getPrestamos(
        @Header("Authorization") token: String
    ): Response<List<PrestamoResponse>>
    
    @GET("prestamos")
    suspend fun getPrestamosByUserId(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int,
        @Query("limit") limit: Int? = null
    ): Response<List<PrestamoResponse>>
    
    @POST("prestamos")
    suspend fun createPrestamo(
        @Header("Authorization") token: String,
        @Body request: CreatePrestamoRequest
    ): Response<Unit>
    
    @PATCH("prestamos/{id}/devolver")
    suspend fun devolverPrestamo(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    @DELETE("prestamos/{id}")
    suspend fun deletePrestamo(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}