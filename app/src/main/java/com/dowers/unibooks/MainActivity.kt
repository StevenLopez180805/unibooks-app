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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.ui.screens.LoginScreen
import com.dowers.unibooks.ui.screens.DashboardScreen
import com.dowers.unibooks.ui.screens.BooksScreen
import com.dowers.unibooks.ui.screens.UsersScreen
import com.dowers.unibooks.ui.screens.StudentDashboardScreen
import com.dowers.unibooks.ui.screens.StudentBooksScreen
import com.dowers.unibooks.ui.screens.StudentLoansScreen
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
    var accessToken by remember { mutableStateOf("") }
    val navController = rememberNavController()

    when {
        currentUser?.role == "bibliotecario" -> {
            NavHost(
                navController = navController,
                startDestination = "dashboard"
            ) {
                composable("dashboard") {
                    DashboardScreen(
                        userInfo = currentUser!!,
                        onLogout = {
                            currentUser = null
                            navController.popBackStack("dashboard", inclusive = false)
                        },
                        onShowProfile = {
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToBooks = {
                            navController.navigate("books")
                        },
                        onNavigateToLoans = {
                            println("Navegar a préstamos")
                        },
                        onNavigateToUsers = {
                            navController.navigate("users")
                        }
                    )
                }
                composable("books") {
                    BooksScreen(
                        userInfo = currentUser!!,
                        api = api,
                        accessToken = accessToken,
                        onLogout = {
                            currentUser = null
                            navController.popBackStack("dashboard", inclusive = false)
                        },
                        onShowProfile = {
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToHome = {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { inclusive = false }
                            }
                        },
                        onNavigateToLoans = {
                            println("Navegar a préstamos")
                        },
                        onNavigateToUsers = {
                            navController.navigate("users")
                        }
                    )
                }
                composable("users") {
                    UsersScreen(
                        userInfo = currentUser!!,
                        api = api,
                        accessToken = accessToken,
                        onLogout = {
                            currentUser = null
                            navController.popBackStack("dashboard", inclusive = false)
                        },
                        onShowProfile = {
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToHome = {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { inclusive = false }
                            }
                        },
                        onNavigateToBooks = {
                            navController.navigate("books")
                        },
                        onNavigateToLoans = {
                            println("Navegar a préstamos")
                        }
                    )
                }
            }
        }
        currentUser?.role == "estudiante" -> {
            NavHost(
                navController = navController,
                startDestination = "student_dashboard"
            ) {
                composable("student_dashboard") {
                    StudentDashboardScreen(
                        userInfo = currentUser!!,
                        onLogout = {
                            currentUser = null
                            navController.popBackStack("student_dashboard", inclusive = false)
                        },
                        onShowProfile = {
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToBooks = {
                            navController.navigate("student_books")
                        },
                        onNavigateToLoans = {
                            navController.navigate("student_loans")
                        }
                    )
                }
                composable("student_books") {
                    StudentBooksScreen(
                        userInfo = currentUser!!,
                        api = api,
                        accessToken = accessToken,
                        onLogout = {
                            currentUser = null
                            navController.popBackStack("student_dashboard", inclusive = false)
                        },
                        onShowProfile = {
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToHome = {
                            navController.navigate("student_dashboard") {
                                popUpTo("student_dashboard") { inclusive = false }
                            }
                        },
                        onNavigateToLoans = {
                            navController.navigate("student_loans")
                        }
                    )
                }
                composable("student_loans") {
                    StudentLoansScreen(
                        userInfo = currentUser!!,
                        onLogout = {
                            currentUser = null
                            navController.popBackStack("student_dashboard", inclusive = false)
                        },
                        onShowProfile = {
                            println("Mostrar perfil de: ${currentUser!!.name}")
                        },
                        onNavigateToHome = {
                            navController.navigate("student_dashboard") {
                                popUpTo("student_dashboard") { inclusive = false }
                            }
                        },
                        onNavigateToBooks = {
                            navController.navigate("student_books")
                        }
                    )
                }
            }
        }
        else -> {
            LoginScreen(
                api = api,
                onLibrarianLogin = { userInfo, token ->
                    currentUser = userInfo
                    accessToken = token
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