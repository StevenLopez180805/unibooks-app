package com.dowers.unibooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.ui.screens.LoginScreen
import com.dowers.unibooks.ui.screens.DashboardScreen
import com.dowers.unibooks.ui.screens.BooksScreen
import com.dowers.unibooks.ui.theme.UnibooksTheme
import com.dowers.unibooks.utils.UserInfo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : ComponentActivity() {
    private val api: AuthApi = run {
        // Configurar logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Muestra headers, body, etc.
        }
        
        // Configurar OkHttpClient con logging
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        // Configurar Retrofit con OkHttpClient
        Retrofit.Builder()
            .baseUrl("https://unibooks-production.up.railway.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(AuthApi::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnibooksTheme {
                AppContent(api)
            }
        }
    }
}

@Composable
fun AppContent(api: AuthApi) {
    var currentUser by remember { mutableStateOf<UserInfo?>(null) }
    var currentScreen by remember { mutableStateOf("dashboard") }
    var accessToken by remember { mutableStateOf("") }
    
    when {
        currentUser?.role == "bibliotecario" -> {
            when (currentScreen) {
                "dashboard" -> {
                    DashboardScreen(
                        userInfo = currentUser!!,
                        onLogout = {
                            currentUser = null
                            currentScreen = "dashboard"
                        },
                        onShowProfile = {
                            // TODO: Implementar pantalla de perfil
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToBooks = {
                            currentScreen = "books"
                        },
                        onNavigateToLoans = {
                            // TODO: Implementar pantalla de préstamos
                            println("Navegar a préstamos")
                        },
                        onNavigateToUsers = {
                            // TODO: Implementar pantalla de usuarios
                            println("Navegar a usuarios")
                        }
                    )
                }
                "books" -> {
                    BooksScreen(
                        userInfo = currentUser!!,
                        api = api,
                        accessToken = accessToken,
                        onLogout = {
                            currentUser = null
                            currentScreen = "dashboard"
                        },
                        onShowProfile = {
                            // TODO: Implementar pantalla de perfil
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToHome = {
                            currentScreen = "dashboard"
                        },
                        onNavigateToLoans = {
                            // TODO: Implementar pantalla de préstamos
                            println("Navegar a préstamos")
                        },
                        onNavigateToUsers = {
                            // TODO: Implementar pantalla de usuarios
                            println("Navegar a usuarios")
                        }
                    )
                }
            }
        }
        currentUser?.role == "estudiante" -> {
            // TODO: Implementar pantalla para estudiantes
            Text(
                text = "Pantalla para estudiantes - ${currentUser!!.name}",
                modifier = Modifier.padding(16.dp)
            )
        }
        else -> {
            LoginScreen(
                api = api,
                onLibrarianLogin = { userInfo, token ->
                    currentUser = userInfo
                    accessToken = token
                    currentScreen = "dashboard"
                },
                onStudentLogin = { userInfo, token ->
                    currentUser = userInfo
                    accessToken = token
                }
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UnibooksTheme {
        Greeting("Steven")
    }
}