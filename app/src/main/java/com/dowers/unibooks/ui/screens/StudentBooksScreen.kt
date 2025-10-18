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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dowers.unibooks.data.models.Book
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.utils.UserInfo
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentBooksScreen(
    userInfo: UserInfo,
    api: AuthApi,
    accessToken: String,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLoans: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showBookDetailsModal by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var allBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoadingBooks by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Cargar libros del servidor
    LaunchedEffect(accessToken) {
        if (accessToken.isNotEmpty()) {
            try {
                Log.d("GET_BOOKS_STUDENT", "Obteniendo libros del servidor")
                isLoadingBooks = true
                errorMessage = null
                
                val response = api.getBooks("Bearer $accessToken")
                
                if (response.isSuccessful) {
                    allBooks = response.body() ?: emptyList()
                    Log.d("GET_BOOKS_STUDENT", "Libros obtenidos: ${allBooks.size}")
                } else {
                    Log.e("GET_BOOKS_STUDENT", "Error obteniendo libros: ${response.code()}")
                    errorMessage = "Error cargando libros: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("GET_BOOKS_STUDENT", "Excepción obteniendo libros: ${e.message}", e)
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoadingBooks = false
            }
        }
    }
    
    // Filtrar libros según la búsqueda
    val filteredBooks = remember(searchQuery, allBooks) {
        val filtered = if (searchQuery.isEmpty()) {
            allBooks
        } else {
            allBooks.filter { book ->
                book.titulo.contains(searchQuery, ignoreCase = true) ||
                book.escritor.contains(searchQuery, ignoreCase = true)
            }
        }
        Log.d("FILTER_BOOKS_STUDENT", "allBooks size: ${allBooks.size}, filteredBooks size: ${filtered.size}")
        filtered
    }

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
                        text = "Catálogo de Libros",
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
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título de la sección
        Text(
            text = "📚 Biblioteca Disponible",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por título o escritor") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar búsqueda"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )

                // Lista de libros
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 150.dp) // Padding para evitar solapamiento con navegación
                    ) {
                    Log.d("LAZY_COLUMN_STUDENT", "Renderizando LazyColumn - isLoadingBooks: $isLoadingBooks, errorMessage: $errorMessage, filteredBooks: ${filteredBooks.size}")
                    
                    if (isLoadingBooks) {
                        item {
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
                        }
                    } else if (errorMessage != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = errorMessage!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            // Recargar libros
                                            scope.launch {
                                                try {
                                                    isLoadingBooks = true
                                                    errorMessage = null
                                                    val response = api.getBooks("Bearer $accessToken")
                                                    if (response.isSuccessful) {
                                                        allBooks = response.body() ?: emptyList()
                                                    } else {
                                                        errorMessage = "Error cargando libros: ${response.code()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error de conexión: ${e.message}"
                                                } finally {
                                                    isLoadingBooks = false
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                    } else if (filteredBooks.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Sin libros",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (searchQuery.isEmpty()) "No hay libros disponibles" else "No se encontraron libros",
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        Log.d("LAZY_COLUMN_STUDENT", "Renderizando ${filteredBooks.size} libros")
                        items(filteredBooks) { book ->
                            Log.d("LAZY_COLUMN_STUDENT", "Renderizando item para libro: ${book.titulo}")
                            StudentBookCard(
                                book = book,
                                onClick = {
                                    selectedBook = book
                                    showBookDetailsModal = true
                                }
                            )
                        }
                    }
                }
            }

        }

        // Barra de navegación inferior (sin acceso a usuarios)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
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
                    isSelected = false,
                    onClick = onNavigateToLoans
                )
                StudentNavigationButton(
                    icon = Icons.Default.Settings,
                    label = "Libros",
                    isSelected = true,
                    onClick = { /* Ya estamos aquí */ }
                )
            }
        }
    }

    // Modal para ver detalles del libro
    if (showBookDetailsModal && selectedBook != null) {
        StudentBookDetailsModal(
            book = selectedBook!!,
            onDismiss = { 
                showBookDetailsModal = false
                selectedBook = null
            }
        )
    }
}

@Composable
fun StudentBookCard(
    book: Book,
    onClick: () -> Unit
) {
    Log.d("STUDENT_BOOK_CARD", "Renderizando StudentBookCard para: ${book.titulo}")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título y stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = book.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Indicador de disponibilidad
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            book.stock == 0 -> MaterialTheme.colorScheme.errorContainer
                            book.stock <= 2 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = when {
                            book.stock == 0 -> "❌ Agotado"
                            book.stock <= 2 -> "⚠️ Pocos ejemplares"
                            else -> "✅ Disponible"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            book.stock == 0 -> MaterialTheme.colorScheme.onErrorContainer
                            book.stock <= 2 -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Escritor
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Escritor",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = book.escritor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ubicación
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = book.ubicacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            if (book.descripcion.isNotEmpty()) {
                Text(
                    text = book.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información adicional para estudiantes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock: ${book.stock} ejemplares",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                if (book.stock > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "Ver detalles",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentBookDetailsModal(
    book: Book,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header del modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📚 Detalles del Libro",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título del libro
                Text(
                    text = book.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Información del escritor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Escritor",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Escritor: ${book.escritor}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Ubicación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ubicación: ${book.ubicacion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Stock disponible
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Stock",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stock disponible: ${book.stock} ejemplares",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Estado de disponibilidad
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            book.stock == 0 -> MaterialTheme.colorScheme.errorContainer
                            book.stock <= 2 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            book.stock == 0 -> "❌ Libro no disponible - Sin ejemplares"
                            book.stock <= 2 -> "⚠️ Pocos ejemplares disponibles"
                            else -> "✅ Libro disponible para préstamo"
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            book.stock == 0 -> MaterialTheme.colorScheme.onErrorContainer
                            book.stock <= 2 -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Descripción si existe
                if (book.descripcion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Descripción:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = book.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón cerrar
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}
