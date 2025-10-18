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
fun LibrarianLoansScreen(
    userInfo: UserInfo,
    api: AuthApi,
    accessToken: String,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToBooks: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var showCreatePrestamoModal by remember { mutableStateOf(false) }
    var showPrestamoDetails by remember { mutableStateOf(false) }
    var selectedPrestamo by remember { mutableStateOf<PrestamoDetalle?>(null) }
    var allBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var allPrestamos by remember { mutableStateOf<List<PrestamoDetalle>>(emptyList()) }
    var isLoadingBooks by remember { mutableStateOf(true) }
    var isLoadingUsers by remember { mutableStateOf(true) }
    var isLoadingPrestamos by remember { mutableStateOf(true) }
    var isLoadingAction by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Cargar libros y usuarios del servidor
    LaunchedEffect(accessToken) {
        if (accessToken.isNotEmpty()) {
            try {
                Log.d("GET_DATA_FOR_LOANS", "Obteniendo datos para préstamos")
                isLoadingBooks = true
                isLoadingUsers = true
                isLoadingPrestamos = true
                
                // Cargar libros
                val booksResponse = api.getBooks("Bearer $accessToken")
                if (booksResponse.isSuccessful) {
                    allBooks = booksResponse.body() ?: emptyList()
                    Log.d("GET_DATA_FOR_LOANS", "Libros obtenidos: ${allBooks.size}")
                } else {
                    Log.e("GET_DATA_FOR_LOANS", "Error obteniendo libros: ${booksResponse.code()}")
                }
                
                // Cargar usuarios
                val usersResponse = api.getUsers("Bearer $accessToken")
                if (usersResponse.isSuccessful) {
                    allUsers = usersResponse.body() ?: emptyList()
                    Log.d("GET_DATA_FOR_LOANS", "Usuarios obtenidos: ${allUsers.size}")
                } else {
                    Log.e("GET_DATA_FOR_LOANS", "Error obteniendo usuarios: ${usersResponse.code()}")
                }
                
                // Cargar préstamos
                val prestamosResponse = api.getPrestamos("Bearer $accessToken")
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
                    Log.d("GET_DATA_FOR_LOANS", "Préstamos obtenidos: ${allPrestamos.size}")
                } else {
                    Log.e("GET_DATA_FOR_LOANS", "Error obteniendo préstamos: ${prestamosResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("GET_DATA_FOR_LOANS", "Excepción obteniendo datos: ${e.message}", e)
            } finally {
                isLoadingBooks = false
                isLoadingUsers = false
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
                            text = "Préstamos",
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
                        text = "📊 Estadísticas de Préstamos",
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
                                text = "Cargando préstamos...",
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
                            LibrarianLoanCard(
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
                                        text = "No hay préstamos registrados",
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
                  NavigationButton(
                      icon = Icons.Default.Person,
                      label = "Usuarios",
                      isSelected = false,
                      onClick = onNavigateToUsers
                  )
                }
            }
        }
        
        // Botón flotante para crear préstamo
        FloatingActionButton(
            onClick = { showCreatePrestamoModal = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, 0.dp, 16.dp, 150.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Crear préstamo",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Modal para crear préstamo
        if (showCreatePrestamoModal) {
            CreateLibrarianPrestamoModal(
                books = allBooks,
                users = allUsers,
                isLoadingBooks = isLoadingBooks,
                isLoadingUsers = isLoadingUsers,
                onDismiss = { showCreatePrestamoModal = false },
                onCreatePrestamo = { prestamoRequest, onSuccess, onError ->
                    scope.launch {
                        try {
                            if (accessToken.isEmpty()) {
                                onError("Token de acceso no disponible. Inicia sesión nuevamente.")
                                return@launch
                            }
                            
                            Log.d("CREATE_PRESTAMO", "Creando préstamo: $prestamoRequest")
                            Log.d("CREATE_PRESTAMO", "Token: Bearer $accessToken")
                            val response = api.createPrestamo("Bearer $accessToken", prestamoRequest)

                            if (response.isSuccessful) {
                                Log.d("CREATE_PRESTAMO", "Préstamo creado exitosamente")
                                
                                // Actualizar el stock de los libros localmente
                                val updatedBooks = allBooks.map { book ->
                                    if (prestamoRequest.libro.any { it.id == book.id }) {
                                        book.copy(stock = maxOf(0, book.stock - 1))
                                    } else {
                                        book
                                    }
                                }
                                allBooks = updatedBooks
                                
                                // Recargar libros desde el servidor para sincronizar
                                try {
                                    val booksResponse = api.getBooks("Bearer $accessToken")
                                    if (booksResponse.isSuccessful) {
                                        allBooks = booksResponse.body() ?: emptyList()
                                        Log.d("CREATE_PRESTAMO", "Libros actualizados desde el servidor")
                                    }
                                } catch (e: Exception) {
                                    Log.w("CREATE_PRESTAMO", "No se pudieron recargar los libros: ${e.message}")
                                }
                                
                                // Recargar préstamos desde el servidor
                                try {
                                    val prestamosResponse = api.getPrestamos("Bearer $accessToken")
                                    if (prestamosResponse.isSuccessful) {
                                        val prestamosData = prestamosResponse.body() ?: emptyList()
                                        allPrestamos = prestamosData.map { prestamo ->
                                            PrestamoDetalle(
                                                id = prestamo.id.toString(),
                                                libro = prestamo.libro.joinToString(", ") { it.titulo },
                                                estudiante = "${prestamo.user.firstName} ${prestamo.user.lastName}",
                                                fechaPrestamo = prestamo.fechaPrestamo,
                                                fechaDevolucion = prestamo.fechaDevolucion ?: "",
                                                fechaLimite = prestamo.fechaDevolucionEsperada,
                                                estado = if (prestamo.fechaDevolucion != null) "Devuelto" else {
                                                    val fechaLimite = LocalDate.parse(prestamo.fechaDevolucionEsperada)
                                                    val hoy = LocalDate.now()
                                                    if (hoy.isAfter(fechaLimite)) "Vencido" else "Prestado"
                                                },
                                                diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(prestamo.fechaDevolucionEsperada)).toInt()
                                            )
                                        }
                                        Log.d("CREATE_PRESTAMO", "Préstamos actualizados desde el servidor")
                                    }
                                } catch (e: Exception) {
                                    Log.w("CREATE_PRESTAMO", "No se pudieron recargar los préstamos: ${e.message}")
                                }
                                
                                onSuccess()
                            } else {
                                Log.e("CREATE_PRESTAMO", "Error creando préstamo: ${response.code()}")
                                Log.e("CREATE_PRESTAMO", "Response body: ${response.errorBody()?.string()}")
                                Log.e("CREATE_PRESTAMO", "Headers: ${response.headers()}")
                                
                                val errorMsg = when (response.code()) {
                                    403 -> "Error de autorización (403). Verifica que tengas permisos para crear préstamos."
                                    401 -> "No autorizado (401). Tu sesión puede haber expirado."
                                    400 -> "Datos inválidos (400). Verifica la información del préstamo."
                                    409 -> "El usuario seleccionado ya tiene 5 préstamos activos. Debe devolver al menos uno antes de crear un nuevo préstamo."
                                    500 -> "Error interno del servidor (500). Intenta más tarde."
                                    else -> "Error del servidor: ${response.code()}"
                                }
                                onError(errorMsg)
                            }
                        } catch (e: Exception) {
                            Log.e("CREATE_PRESTAMO", "Excepción creando préstamo: ${e.message}", e)
                            onError("Error de conexión: ${e.message}")
                        }
                    }
                }
            )
        }
        
        // Modal para detalles del préstamo
        if (showPrestamoDetails && selectedPrestamo != null) {
            PrestamoDetailsModal(
                prestamo = selectedPrestamo!!,
                onDismiss = { 
                    showPrestamoDetails = false
                    selectedPrestamo = null
                },
                onDevolver = { prestamoId ->
                    scope.launch {
                        try {
                            isLoadingAction = true
                            val response = api.devolverPrestamo("Bearer $accessToken", prestamoId.toInt())
                            if (response.isSuccessful) {
                                Log.d("DEVOLVER_PRESTAMO", "Préstamo devuelto exitosamente")
                                
                                // Recargar libros desde el servidor para sincronizar
                                try {
                                    val booksResponse = api.getBooks("Bearer $accessToken")
                                    if (booksResponse.isSuccessful) {
                                        allBooks = booksResponse.body() ?: emptyList()
                                        Log.d("DEVOLVER_PRESTAMO", "Libros actualizados desde el servidor")
                                    }
                                } catch (e: Exception) {
                                    Log.w("DEVOLVER_PRESTAMO", "No se pudieron recargar los libros: ${e.message}")
                                }
                                
                                // Recargar préstamos
                                val prestamosResponse = api.getPrestamos("Bearer $accessToken")
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
                                }
                                showPrestamoDetails = false
                                selectedPrestamo = null
                            } else {
                                Log.e("DEVOLVER_PRESTAMO", "Error devolviendo préstamo: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("DEVOLVER_PRESTAMO", "Excepción devolviendo préstamo: ${e.message}", e)
                        } finally {
                            isLoadingAction = false
                        }
                    }
                },
                onEliminar = { prestamoId ->
                    scope.launch {
                        try {
                            isLoadingAction = true
                            val response = api.deletePrestamo("Bearer $accessToken", prestamoId.toInt())
                            if (response.isSuccessful) {
                                Log.d("DELETE_PRESTAMO", "Préstamo eliminado exitosamente")
                                
                                // Recargar libros desde el servidor para sincronizar
                                try {
                                    val booksResponse = api.getBooks("Bearer $accessToken")
                                    if (booksResponse.isSuccessful) {
                                        allBooks = booksResponse.body() ?: emptyList()
                                        Log.d("DELETE_PRESTAMO", "Libros actualizados desde el servidor")
                                    }
                                } catch (e: Exception) {
                                    Log.w("DELETE_PRESTAMO", "No se pudieron recargar los libros: ${e.message}")
                                }
                                
                                // Recargar préstamos
                                val prestamosResponse = api.getPrestamos("Bearer $accessToken")
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
                                }
                                showPrestamoDetails = false
                                selectedPrestamo = null
                            } else {
                                Log.e("DELETE_PRESTAMO", "Error eliminando préstamo: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("DELETE_PRESTAMO", "Excepción eliminando préstamo: ${e.message}", e)
                        } finally {
                            isLoadingAction = false
                        }
                    }
                },
                isLoading = isLoadingAction
            )
        }
    }
}

@Composable
fun LibrarianLoanCard(
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
            
            // Información del estudiante
            Text(
                text = "👤 ${prestamo.estudiante}",
                style = MaterialTheme.typography.bodyMedium,
                color = when (prestamo.estado) {
                    "Prestado" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "Vencido" -> MaterialTheme.colorScheme.onErrorContainer
                    "Devuelto" -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
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
fun CreateLibrarianPrestamoModal(
    books: List<Book>,
    users: List<User>,
    isLoadingBooks: Boolean,
    isLoadingUsers: Boolean,
    onDismiss: () -> Unit,
    onCreatePrestamo: (CreatePrestamoRequest, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    var selectedBooks by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserDropdown by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Contenido scrolleable
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Título del modal
                        Text(
                            text = "📚 Crear Nuevo Préstamo",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    item {
                        // Información sobre tiempo máximo
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Información",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "El tiempo máximo de devolución es de 30 días",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    
                    item {
                        // Selector de usuario mejorado
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "👤 Selecciona el estudiante:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box {
                                    OutlinedTextField(
                                        value = selectedUser?.let { "${it.firstName} ${it.lastName}" } ?: "",
                                        onValueChange = { },
                                        label = { 
                                            Text(
                                                "Estudiante",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        readOnly = true,
                                        enabled = !isLoadingUsers,
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Usuario",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { showUserDropdown = true },
                                                enabled = !isLoadingUsers
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowDropDown, 
                                                    contentDescription = "Seleccionar usuario",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                if (!isLoadingUsers) {
                                                    showUserDropdown = true 
                                                }
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    
                                    DropdownMenu(
                                        expanded = showUserDropdown,
                                        onDismissRequest = { showUserDropdown = false },
                                        modifier = Modifier
                                            .widthIn(min = 0.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        if (isLoadingUsers) {
                                            DropdownMenuItem(
                                                text = { 
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(16.dp),
                                                            strokeWidth = 2.dp
                                                        )
                                                        Text("Cargando usuarios...")
                                                    }
                                                },
                                                onClick = { }
                                            )
                                        } else if (users.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { 
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = "Sin usuarios",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text("No hay usuarios disponibles")
                                                    }
                                                },
                                                onClick = { }
                                            )
                                        } else {
                                            val estudiantes = users.filter { it.role == "estudiante" }
                                            if (estudiantes.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { 
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Person,
                                                                contentDescription = "Sin estudiantes",
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Text("No hay estudiantes registrados")
                                                        }
                                                    },
                                                    onClick = { }
                                                )
                                            } else {
                                                estudiantes.forEach { user ->
                                                    DropdownMenuItem(
                                                        text = { 
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Card(
                                                                    modifier = Modifier.size(32.dp),
                                                                    colors = CardDefaults.cardColors(
                                                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                                                    ),
                                                                    shape = RoundedCornerShape(16.dp)
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Text(
                                                                            text = "${user.firstName.first()}${user.lastName.first()}",
                                                                            style = MaterialTheme.typography.labelMedium,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                                        )
                                                                    }
                                                                }
                                                                
                                                                Column {
                                                                    Text(
                                                                        text = "${user.firstName} ${user.lastName}",
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        fontWeight = FontWeight.Medium
                                                                    )
                                                                    Text(
                                                                        text = user.email,
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            }
                                                        },
                                                        onClick = {
                                                            selectedUser = user
                                                            showUserDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Información del usuario seleccionado
                    if (selectedUser != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.size(40.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${selectedUser!!.firstName.first()}${selectedUser!!.lastName.first()}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                    
                                    Column {
                                        Text(
                                            text = "✅ Estudiante seleccionado",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${selectedUser!!.firstName} ${selectedUser!!.lastName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = selectedUser!!.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        // Selector de libros
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Selecciona los libros a prestar:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (isLoadingBooks) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator()
                                        Text("Cargando libros...")
                                    }
                                }
                            } else {
                                val availableBooks = books.filter { it.stock > 0 }
                                if (availableBooks.isEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "No hay libros disponibles con stock",
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(availableBooks) { book ->
                                            BookSelectionItem(
                                                book = book,
                                                isSelected = selectedBooks.contains(book.id),
                                                onToggle = { bookId: Int ->
                                                    selectedBooks = if (selectedBooks.contains(bookId)) {
                                                        selectedBooks - bookId
                                                    } else {
                                                        selectedBooks + bookId
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Botones de acción (fuera del scroll)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (selectedBooks.isNotEmpty() && selectedUser != null) {
                                // Validar que todos los libros seleccionados tengan stock
                                val selectedBooksData = books.filter { selectedBooks.contains(it.id) }
                                val booksWithoutStock = selectedBooksData.filter { it.stock <= 0 }
                                
                                if (booksWithoutStock.isNotEmpty()) {
                                    errorMessage = "Los siguientes libros no tienen stock disponible:\n" +
                                            booksWithoutStock.joinToString("\n") { "- ${it.titulo}" }
                                    showErrorDialog = true
                                    return@Button
                                }
                                
                                isLoading = true
                                
                                val today = LocalDate.now()
                                val returnDate = today.plusDays(30)
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                
                                val prestamoRequest = CreatePrestamoRequest(
                                    fechaPrestamo = today.format(formatter),
                                    fechaDevolucionEsperada = returnDate.format(formatter),
                                    fechaDevolucion = null,
                                    user = UserRef(id = selectedUser!!.id),
                                    libro = selectedBooks.map { LibroRef(id = it) }
                                )
                                
                                onCreatePrestamo(
                                    prestamoRequest,
                                    {
                                        isLoading = false
                                        onDismiss()
                                    },
                                    { error ->
                                        isLoading = false
                                        errorMessage = error
                                        showErrorDialog = true
                                    }
                                )
                            }
                        },
                        enabled = selectedBooks.isNotEmpty() && selectedUser != null && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirmar")
                        }
                    }
                }
            }
        }
        
        // Diálogo de error dentro del modal
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = {
                    Text(
                        text = "Error al crear préstamo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(errorMessage)
                },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false }
                    ) {
                        Text("Aceptar")
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}


@Composable
fun BookSelectionItem(
    book: Book,
    isSelected: Boolean,
    onToggle: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(book.id) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle(book.id) }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Text(
                    text = "por ${book.escritor}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock: ${book.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            book.stock == 0 -> MaterialTheme.colorScheme.error
                            book.stock <= 2 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "• ${book.ubicacion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PrestamoDetailsModal(
    prestamo: PrestamoDetalle,
    onDismiss: () -> Unit,
    onDevolver: (String) -> Unit,
    onEliminar: (String) -> Unit,
    isLoading: Boolean
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
                        
                        // Información del estudiante
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
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Estudiante",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = prestamo.estudiante,
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
                }
                
                // Botones de acción (como item del LazyColumn)
                item {
                    if (prestamo.estado != "Devuelto") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onEliminar(prestamo.id) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Eliminar")
                                }
                            }
                            
                            Button(
                                onClick = { onDevolver(prestamo.id) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Confirmar Devolución")
                                }
                            }
                        }
                    } else {
                        // Solo botón de cerrar si ya está devuelto
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
}}}
