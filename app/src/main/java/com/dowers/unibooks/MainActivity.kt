package com.dowers.unibooks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.ui.screens.LoginScreen
import com.dowers.unibooks.ui.theme.UnibooksTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    private val api: AuthApi = Retrofit.Builder()
        .baseUrl("https://tu-api.com/") // <-- tu URL base
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnibooksTheme {
                LoginScreen(api) { token ->
                    println("Login exitoso! Token: $token")
                }
            }
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