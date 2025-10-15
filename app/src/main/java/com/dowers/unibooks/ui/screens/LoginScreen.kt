package com.dowers.unibooks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.data.remote.LoginRequest
import com.dowers.unibooks.utils.JwtDecoder
import com.dowers.unibooks.utils.UserInfo
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun LoginScreen(
    api: AuthApi, 
    onLibrarianLogin: (UserInfo, String) -> Unit,
    onStudentLogin: (UserInfo, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null // Limpiar mensajes de error previos
                    
                    try {
                        Log.d("LOGIN", "Intentando login con email: $email")
                        val request = LoginRequest(email, password)
                        Log.d("LOGIN", "Request enviado: $request")
                        
                        val response = api.login(request)
                        
                        Log.d("LOGIN", "Respuesta recibida:")
                        Log.d("LOGIN", "- Status Code: ${response.code()}")
                        Log.d("LOGIN", "- Is Successful: ${response.isSuccessful}")
                        Log.d("LOGIN", "- Headers: ${response.headers()}")
                        
                        val responseBody = response.body()
                        Log.d("LOGIN", "- Parsed Body: $responseBody")
                        
                        if (response.isSuccessful && response.code() == 201) {
                            responseBody?.let { loginResponse ->
                                Log.d("LOGIN", "Login exitoso! Token: ${loginResponse.access_token}")
                                
                                // Decodificar JWT para obtener información del usuario
                                val userInfo = JwtDecoder.decodeToken(loginResponse.access_token)
                                
                                if (userInfo != null) {
                                    Log.d("LOGIN", "Usuario decodificado: $userInfo")
                                    
                                    // Redirigir según el rol
                                    if (userInfo.role == "bibliotecario") {
                                        onLibrarianLogin(userInfo, loginResponse.access_token)
                                    } else {
                                        onStudentLogin(userInfo, loginResponse.access_token)
                                    }
                                } else {
                                    Log.e("LOGIN", "Error decodificando JWT")
                                    errorMessage = "Error: Token inválido"
                                }
                            } ?: run {
                                Log.e("LOGIN", "Response body es null")
                                errorMessage = "Error: Respuesta vacía del servidor"
                            }
                        } else {
                            Log.e("LOGIN", "Login falló - Status: ${response.code()}")
                            errorMessage = "Credenciales inválidas (Status: ${response.code()})"
                        }
                    } catch (e: Exception) {
                        Log.e("LOGIN", "Excepción durante login: ${e.message}", e)
                        errorMessage = "Error de conexión: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciando sesión...")
                }
            } else {
                Text("Iniciar sesión")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
