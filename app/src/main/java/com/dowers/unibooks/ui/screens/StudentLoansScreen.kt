package com.dowers.unibooks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import com.dowers.unibooks.utils.UserInfo

data class PrestamoEstudianteDetalle(
    val id: String,
    val libro: String,
    val fechaPrestamo: String,
    val fechaDevolucion: String,
    val fechaLimite: String,
    val estado: String,
    val diasRestantes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLoansScreen(
    userInfo: UserInfo,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToBooks: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }
    
    // Datos de ejemplo para los préstamos del estudiante con más detalles
    val prestamosEstudiante = remember {
        listOf(
            PrestamoEstudianteDetalle("1", "El Quijote", "2024-01-15", "2024-01-22", "2024-01-29", "Prestado", 3),
            PrestamoEstudianteDetalle("2", "Cien años de soledad", "2024-01-14", "2024-01-21", "2024-01-28", "Prestado", 2),
            PrestamoEstudianteDetalle("3", "1984", "2024-01-13", "2024-01-20", "2024-01-27", "Devuelto", 0),
            PrestamoEstudianteDetalle("4", "Don Juan Tenorio", "2024-01-10", "2024-01-17", "2024-01-24", "Vencido", -3)
        )
    }

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
                containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = userInfo.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Box {
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        IconButton(
                            onClick = { showUserMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Menú de usuario",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver perfil") },
                            onClick = {
                                showUserMenu = false
                                onShowProfile()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📊 Resumen de Préstamos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        title = "Activos",
                        value = prestamosEstudiante.count { it.estado == "Prestado" }.toString(),
                        icon = Icons.Default.Settings,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    StatCard(
                        title = "Vencidos",
                        value = prestamosEstudiante.count { it.estado == "Vencido" }.toString(),
                        icon = Icons.Default.Person,
                        color = MaterialTheme.colorScheme.errorContainer
                    )
                    StatCard(
                        title = "Devueltos",
                        value = prestamosEstudiante.count { it.estado == "Devuelto" }.toString(),
                        icon = Icons.Default.AccountCircle,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título de la sección
        Text(
            text = "📋 Historial de Préstamos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Lista de préstamos del estudiante
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
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(prestamosEstudiante) { prestamo ->
                    StudentLoanCard(prestamo = prestamo)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de navegación inferior
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StudentNavigationButton(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    isSelected = false,
                    onClick = onNavigateToHome
                )
                StudentNavigationButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Préstamos",
                    isSelected = true,
                    onClick = { /* Ya estamos aquí */ }
                )
                StudentNavigationButton(
                    icon = Icons.Default.Settings,
                    label = "Libros",
                    isSelected = false,
                    onClick = onNavigateToBooks
                )
            }
        }
    }
}

@Composable
fun StudentLoanCard(
    prestamo: PrestamoEstudianteDetalle
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
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
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = prestamo.libro,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (prestamo.estado) {
                            "Prestado" -> MaterialTheme.colorScheme.primary
                            "Vencido" -> MaterialTheme.colorScheme.error
                            "Devuelto" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = when (prestamo.estado) {
                            "Prestado" -> "⏳ Prestado"
                            "Vencido" -> "⚠️ Vencido"
                            "Devuelto" -> "✅ Devuelto"
                            else -> prestamo.estado
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (prestamo.estado) {
                            "Prestado" -> MaterialTheme.colorScheme.onPrimary
                            "Vencido" -> MaterialTheme.colorScheme.onError
                            "Devuelto" -> MaterialTheme.colorScheme.onTertiary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Fecha de préstamo:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = prestamo.fechaPrestamo,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "Fecha límite:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = prestamo.fechaLimite,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
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
                                "📅 Te quedan ${prestamo.diasRestantes} días para devolver el libro"
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
                            text = "⚠️ El libro está vencido desde hace ${kotlin.math.abs(prestamo.diasRestantes)} días",
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
                            text = "✅ Devuelto el ${prestamo.fechaDevolucion}",
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
