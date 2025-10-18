package com.dowers.unibooks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dowers.unibooks.data.models.Book
import com.dowers.unibooks.data.models.User
import com.dowers.unibooks.data.models.PrestamoDetalle
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.data.remote.CreatePrestamoRequest
import com.dowers.unibooks.data.remote.UserRef
import com.dowers.unibooks.data.remote.LibroRef
import com.dowers.unibooks.utils.UserInfo
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.util.Log

@Composable
fun StudentLoansScreen(
    userInfo: UserInfo,
    api: AuthApi,
    accessToken: String,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToBooks: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var showPrestamoDetails by remember { mutableStateOf(false) }
    var selectedPrestamo by remember { mutableStateOf<PrestamoDetalle?>(null) }
    var allPrestamos by remember { mutableStateOf<List<PrestamoDetalle>>(emptyList()) }
    var isLoadingPrestamos by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Cargar préstamos del estudiante desde el servidor
    LaunchedEffect(accessToken, userInfo.id) {
        if (accessToken.isNotEmpty() && userInfo.id > 0) {
            try {
                Log.d("GET_STUDENT_LOANS", "Obteniendo préstamos del estudiante: ${userInfo.id}")
                isLoadingPrestamos = true
                
                // Cargar préstamos del estudiante específico
                val prestamosResponse = api.getPrestamosByUserId("Bearer $accessToken", userInfo.id)
                if (prestamosResponse.isSuccessful) {
                    val prestamosData = prestamosResponse.body() ?: emptyList()
                    allPrestamos = prestamosData.map { prestamo ->
                        val libro = prestamo.libro.firstOrNull()
                        val fechaLimite = prestamo.fechaDevolucionEsperada
                        val fechaActual = LocalDate.now()
                        val fechaLimiteDate = LocalDate.parse(fechaLimite)
                        val diasRestantes = ChronoUnit.DAYS.between(fechaActual, fechaLimiteDate).toInt()
                        
                        val estado = when {
                            prestamo.fechaDevolucion != null -> "Devuelto"
                            diasRestantes < 0 -> "Vencido"
                            else -> "Prestado"
                        }
                        
                        PrestamoDetalle(
                            id = prestamo.id.toString(),
                            libro = libro?.titulo ?: "Libro no encontrado",
                            estudiante = "${prestamo.user.firstName} ${prestamo.user.lastName}",
                            fechaPrestamo = prestamo.fechaPrestamo,
                            fechaDevolucion = prestamo.fechaDevolucion ?: "",
                            fechaLimite = fechaLimite,
                            estado = estado,
                            diasRestantes = diasRestantes
                        )
                    }
                    Log.d("GET_STUDENT_LOANS", "Préstamos obtenidos: ${allPrestamos.size}")
                } else {
                    Log.e("GET_STUDENT_LOANS", "Error obteniendo préstamos: ${prestamosResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("GET_STUDENT_LOANS", "Excepción obteniendo préstamos: ${e.message}", e)
            } finally {
                isLoadingPrestamos = false
            }
        }
    }
    
    // Usar datos reales de la API
    val prestamos = allPrestamos

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header con nombre del usuario y menú
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mis Préstamos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = userInfo.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Box {
                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            IconButton(
                                onClick = { showUserMenu = true }
                            ) {
                              Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Menú de usuario",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                              )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                onClick = {
                                    showUserMenu = false
                                    onShowProfile()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar Sesión") },
                                onClick = {
                                    showUserMenu = false
                                    onLogout()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estadísticas de préstamos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📊 Mis Estadísticas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(
                            title = "Activos",
                            value = if (isLoadingPrestamos) "..." else prestamos.count { it.estado == "Prestado" }.toString(),
                            icon = Icons.Default.List,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        StatCard(
                            title = "Vencidos",
                            value = if (isLoadingPrestamos) "..." else prestamos.count { it.estado == "Vencido" }.toString(),
                            icon = Icons.Default.Warning,
                            color = MaterialTheme.colorScheme.errorContainer
                        )
                        StatCard(
                            title = "Devueltos",
                            value = if (isLoadingPrestamos) "..." else prestamos.count { it.estado == "Devuelto" }.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de préstamos
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                if (isLoadingPrestamos) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Cargando tus préstamos...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prestamos) { prestamo ->
                            StudentLoanCard(
                                prestamo = prestamo,
                                onClick = {
                                    selectedPrestamo = prestamo
                                    showPrestamoDetails = true
                                }
                            )
                        }
                        
                        if (prestamos.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = "No tienes préstamos registrados",
                                        modifier = Modifier.padding(24.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de navegación inferior
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                  NavigationButton(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    isSelected = false,
                    onClick = { onNavigateToHome() }
                  )
                  NavigationButton(
                      icon = Icons.AutoMirrored.Filled.List,
                      label = "Préstamos",
                      isSelected = true,
                      onClick = { /* Ya estamos en loans */ }
                  )
                  NavigationButton(
                      icon = Icons.Default.Settings,
                      label = "Libros",
                      isSelected = false,
                      onClick = onNavigateToBooks
                  )
                }
            }
        }
        
        // Modal para detalles del préstamo (solo lectura para estudiantes)
        if (showPrestamoDetails && selectedPrestamo != null) {
            StudentPrestamoDetailsModal(
                prestamo = selectedPrestamo!!,
                onDismiss = { 
                    showPrestamoDetails = false
                    selectedPrestamo = null
                }
            )
        }
    }
}

@Composable
fun StudentLoanCard(
    prestamo: PrestamoDetalle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (prestamo.estado) {
                "Prestado" -> MaterialTheme.colorScheme.primaryContainer
                "Vencido" -> MaterialTheme.colorScheme.errorContainer
                "Devuelto" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título del libro y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prestamo.libro,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (prestamo.estado) {
                        "Prestado" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                        "Devuelto" -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (prestamo.estado) {
                            "Prestado" -> MaterialTheme.colorScheme.primary
                            "Vencido" -> MaterialTheme.colorScheme.error
                            "Devuelto" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = prestamo.estado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = when (prestamo.estado) {
                            "Prestado" -> MaterialTheme.colorScheme.onPrimary
                            "Vencido" -> MaterialTheme.colorScheme.onError
                            "Devuelto" -> MaterialTheme.colorScheme.onTertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Fechas
            Text(
                text = "📅 Prestado: ${prestamo.fechaPrestamo}",
                style = MaterialTheme.typography.bodySmall,
                color = when (prestamo.estado) {
                    "Prestado" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                    "Devuelto" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Text(
                text = "⏰ Límite: ${prestamo.fechaLimite}",
                style = MaterialTheme.typography.bodySmall,
                color = when (prestamo.estado) {
                    "Prestado" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                    "Devuelto" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            if (prestamo.fechaDevolucion.isNotEmpty()) {
                Text(
                    text = "✅ Devuelto: ${prestamo.fechaDevolucion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            // Información adicional según el estado
            when (prestamo.estado) {
                "Prestado" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (prestamo.diasRestantes <= 2) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (prestamo.diasRestantes > 0) {
                                "📅 Quedan ${prestamo.diasRestantes} días para devolver el libro"
                            } else {
                                "⚠️ El libro debe ser devuelto hoy"
                            },
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (prestamo.diasRestantes <= 2) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                "Vencido" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ El libro está vencido desde hace ${-prestamo.diasRestantes} días",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                "Devuelto" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ Libro devuelto correctamente",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentPrestamoDetailsModal(
    prestamo: PrestamoDetalle,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Título del modal
                Text(
                    text = "📋 Detalles del Préstamo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Contenido scrolleable
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Información del préstamo
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                        // Información del libro
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Libro",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = prestamo.libro,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        // Fecha de préstamo
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Fecha de préstamo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = prestamo.fechaPrestamo,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // Fecha límite
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Fecha límite",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = prestamo.fechaLimite,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // Fecha de devolución (si existe)
                        if (prestamo.fechaDevolucion.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Fecha de devolución",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = prestamo.fechaDevolucion,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Estado del préstamo
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when (prestamo.estado) {
                                    "Prestado" -> MaterialTheme.colorScheme.primaryContainer
                                    "Vencido" -> MaterialTheme.colorScheme.errorContainer
                                    "Devuelto" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (prestamo.estado) {
                                        "Devuelto" -> Icons.Default.CheckCircle
                                        "Vencido" -> Icons.Default.Warning
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when (prestamo.estado) {
                                        "Prestado" -> MaterialTheme.colorScheme.onPrimaryContainer
                                        "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                                        "Devuelto" -> MaterialTheme.colorScheme.onTertiaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Estado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (prestamo.estado) {
                                            "Prestado" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                                            "Devuelto" -> MaterialTheme.colorScheme.onTertiaryContainer
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    Text(
                                        text = prestamo.estado,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = when (prestamo.estado) {
                                            "Prestado" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                                            "Devuelto" -> MaterialTheme.colorScheme.onTertiaryContainer
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                if (prestamo.estado != "Devuelto") {
                                    Text(
                                        text = "${prestamo.diasRestantes} días",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when (prestamo.estado) {
                                            "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Solo botón de cerrar (estudiantes no pueden realizar acciones)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}
}
}