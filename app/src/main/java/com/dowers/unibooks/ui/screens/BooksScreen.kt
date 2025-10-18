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
import androidx.compose.ui.window.Dialog
import com.dowers.unibooks.data.models.Book
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.data.remote.CreateBookRequest
import com.dowers.unibooks.utils.UserInfo
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    userInfo: UserInfo,
    api: AuthApi,
    accessToken: String,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateBookModal by remember { mutableStateOf(false) }
    var showEditBookModal by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var allBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoadingBooks by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Cargar libros del servidor
    LaunchedEffect(accessToken) {
        if (accessToken.isNotEmpty()) {
            try {
                Log.d("GET_BOOKS", "Obteniendo libros del servidor")
                isLoadingBooks = true
                errorMessage = null
                
                val response = api.getBooks("Bearer $accessToken")
                
                if (response.isSuccessful) {
                    allBooks = response.body() ?: emptyList()
                    Log.d("GET_BOOKS", "Libros obtenidos: ${allBooks.size}")
                    Log.d("GET_BOOKS", "Primer libro: ${if (allBooks.isNotEmpty()) allBooks[0] else "Lista vacía"}")
                } else {
                    Log.e("GET_BOOKS", "Error obteniendo libros: ${response.code()}")
                    errorMessage = "Error cargando libros: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("GET_BOOKS", "Excepción obteniendo libros: ${e.message}", e)
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
        Log.d("FILTER_BOOKS", "allBooks size: ${allBooks.size}, filteredBooks size: ${filtered.size}")
        Log.d("FILTER_BOOKS", "isLoadingBooks: $isLoadingBooks, errorMessage: $errorMessage")
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
        // Header con nombre del usuario y menú (reutilizado)
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
                        text = "Biblioteca",
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
            text = "📚 Catálogo de Libros",
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
                    Log.d("LAZY_COLUMN", "Renderizando LazyColumn - isLoadingBooks: $isLoadingBooks, errorMessage: $errorMessage, filteredBooks: ${filteredBooks.size}")
                    
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
                        Log.d("LAZY_COLUMN", "Renderizando ${filteredBooks.size} libros")
                        items(filteredBooks) { book ->
                            Log.d("LAZY_COLUMN", "Renderizando item para libro: ${book.titulo}")
                            BookCard(
                                book = book,
                                onClick = {
                                    selectedBook = book
                                    showEditBookModal = true
                                },
                                onDelete = { bookToDelete ->
                                    scope.launch {
                                        try {
                                            Log.d("DELETE_BOOK", "Eliminando libro ${bookToDelete.id}: ${bookToDelete.titulo}")
                                            val response = api.deleteBook("Bearer $accessToken", bookToDelete.id)

                                            if (response.isSuccessful) {
                                                Log.d("DELETE_BOOK", "Libro eliminado exitosamente")
                                                
                                                // Recargar la lista de libros
                                                try {
                                                    val booksResponse = api.getBooks("Bearer $accessToken")
                                                    if (booksResponse.isSuccessful) {
                                                        allBooks = booksResponse.body() ?: emptyList()
                                                        Log.d("DELETE_BOOK", "Lista de libros actualizada: ${allBooks.size}")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("DELETE_BOOK", "Error recargando libros: ${e.message}")
                                                }
                                            } else {
                                                Log.e("DELETE_BOOK", "Error eliminando libro: ${response.code()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("DELETE_BOOK", "Excepción eliminando libro: ${e.message}", e)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

        }

        // FloatingActionButton para crear libro
        FloatingActionButton(
            onClick = { showCreateBookModal = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, 0.dp, 16.dp, 150.dp), // Padding inferior para evitar solapamiento con navegación
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear libro",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Barra de navegación inferior (reutilizada)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    onClick = onNavigateToHome
                )
                NavigationButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Préstamos",
                    isSelected = false,
                    onClick = onNavigateToLoans
                )
                NavigationButton(
                    icon = Icons.Default.Settings,
                    label = "Libros",
                    isSelected = true,
                    onClick = { /* Ya estamos aquí */ }
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

        // Modal para crear libro
        if (showCreateBookModal) {
        CreateBookModal(
            onDismiss = { showCreateBookModal = false },
                    onCreateBook = { bookRequest ->
                        scope.launch {
                            try {
                                Log.d("CREATE_BOOK", "Creando libro: $bookRequest")
                                val response = api.createBook("Bearer $accessToken", bookRequest)

                                if (response.isSuccessful) {
                                    Log.d("CREATE_BOOK", "Libro creado exitosamente: ${response.body()}")
                                    showCreateBookModal = false
                                    
                                    // Recargar la lista de libros
                                    try {
                                        val booksResponse = api.getBooks("Bearer $accessToken")
                                        if (booksResponse.isSuccessful) {
                                            allBooks = booksResponse.body() ?: emptyList()
                                            Log.d("CREATE_BOOK", "Lista de libros actualizada: ${allBooks.size}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("CREATE_BOOK", "Error recargando libros: ${e.message}")
                                    }
                                } else {
                                    Log.e("CREATE_BOOK", "Error creando libro: ${response.code()}")
                                }
                            } catch (e: Exception) {
                                Log.e("CREATE_BOOK", "Excepción creando libro: ${e.message}", e)
                            }
                        }
                    }
        )
    }

        // Modal para editar libro
        if (showEditBookModal && selectedBook != null) {
            EditBookModal(
                book = selectedBook!!,
                onDismiss = { 
                    showEditBookModal = false
                    selectedBook = null
                },
                onUpdateBook = { bookId, bookRequest ->
                    scope.launch {
                        try {
                            Log.d("UPDATE_BOOK", "Actualizando libro $bookId: $bookRequest")
                            val response = api.updateBook("Bearer $accessToken", bookId, bookRequest)

                            if (response.isSuccessful) {
                                Log.d("UPDATE_BOOK", "Libro actualizado exitosamente: ${response.body()}")
                                showEditBookModal = false
                                selectedBook = null
                                
                                // Recargar la lista de libros
                                try {
                                    val booksResponse = api.getBooks("Bearer $accessToken")
                                    if (booksResponse.isSuccessful) {
                                        allBooks = booksResponse.body() ?: emptyList()
                                        Log.d("UPDATE_BOOK", "Lista de libros actualizada: ${allBooks.size}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("UPDATE_BOOK", "Error recargando libros: ${e.message}")
                                }
                            } else {
                                Log.e("UPDATE_BOOK", "Error actualizando libro: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("UPDATE_BOOK", "Excepción actualizando libro: ${e.message}", e)
                        }
                    }
                }
            )
        }
}

@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onDelete: (Book) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Log.d("BOOK_CARD", "Renderizando BookCard para: ${book.titulo}")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título, stock y botón eliminar
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
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicador de stock
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
                            text = "📚 ${book.stock}",
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
                    
                    // Botón eliminar
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar libro",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = book.escritor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = book.ubicacion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            if (book.descripcion.isNotEmpty()) {
                Text(
                    text = book.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Eliminar libro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres eliminar el libro \"${book.titulo}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(book)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

