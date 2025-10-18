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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dowers.unibooks.data.models.User
import com.dowers.unibooks.data.remote.AuthApi
import com.dowers.unibooks.data.remote.CreateUserRequest
import com.dowers.unibooks.utils.UserInfo
import kotlinx.coroutines.launch
import android.util.Log
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    userInfo: UserInfo,
    api: AuthApi,
    accessToken: String,
    onLogout: () -> Unit,
    onShowProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToBooks: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateUserModal by remember { mutableStateOf(false) }
    var showEditUserModal by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoadingUsers by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Cargar usuarios del servidor
    LaunchedEffect(accessToken) {
        if (accessToken.isNotEmpty()) {
            try {
                Log.d("GET_USERS", "Obteniendo usuarios del servidor")
                isLoadingUsers = true
                errorMessage = null
                
                val response = api.getUsers("Bearer $accessToken")
                
                if (response.isSuccessful) {
                    allUsers = response.body() ?: emptyList()
                    Log.d("GET_USERS", "Usuarios obtenidos: ${allUsers.size}")
                    Log.d("GET_USERS", "Primer usuario: ${if (allUsers.isNotEmpty()) allUsers[0] else "Lista vacía"}")
                } else {
                    Log.e("GET_USERS", "Error obteniendo usuarios: ${response.code()}")
                    errorMessage = "Error cargando usuarios: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("GET_USERS", "Excepción obteniendo usuarios: ${e.message}", e)
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoadingUsers = false
            }
        }
    }
    
    // Filtrar usuarios según la búsqueda y excluir el usuario actual
    val filteredUsers = remember(searchQuery, allUsers, userInfo.email) {
        // Primero excluir el usuario actual
        val usersWithoutCurrent = allUsers.filter { user ->
            user.id != userInfo.id
        }
        
        // Luego aplicar filtro de búsqueda
        val filtered = if (searchQuery.isEmpty()) {
            usersWithoutCurrent
        } else {
            usersWithoutCurrent.filter { user ->
                user.firstName.contains(searchQuery, ignoreCase = true) ||
                user.lastName.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.cedula.contains(searchQuery, ignoreCase = true)
            }
        }
        Log.d("FILTER_USERS", "allUsers size: ${allUsers.size}, usersWithoutCurrent: ${usersWithoutCurrent.size}, filteredUsers size: ${filtered.size}")
        Log.d("FILTER_USERS", "isLoadingUsers: $isLoadingUsers, errorMessage: $errorMessage")
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
                        text = "Usuarios",
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
                                Icon(Icons.Default.ExitToApp, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Título de la sección
        Text(
            text = "👥 Gestión de Usuarios",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre o cédula") },
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

                // Lista de usuarios
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 150.dp) // Padding para evitar solapamiento con navegación
                    ) {
                    Log.d("LAZY_COLUMN", "Renderizando LazyColumn - isLoadingUsers: $isLoadingUsers, errorMessage: $errorMessage, filteredUsers: ${filteredUsers.size}")
                    
                    if (isLoadingUsers) {
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
                                    Text("Cargando usuarios...")
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
                                            // Recargar usuarios
                                            scope.launch {
                                                try {
                                                    isLoadingUsers = true
                                                    errorMessage = null
                                                    val response = api.getUsers("Bearer $accessToken")
                                                    if (response.isSuccessful) {
                                                        allUsers = response.body() ?: emptyList()
                                                    } else {
                                                        errorMessage = "Error cargando usuarios: ${response.code()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error de conexión: ${e.message}"
                                                } finally {
                                                    isLoadingUsers = false
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                    } else if (filteredUsers.isEmpty()) {
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
                                        contentDescription = "Sin usuarios",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (searchQuery.isEmpty()) "No hay usuarios disponibles" else "No se encontraron usuarios",
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        Log.d("LAZY_COLUMN", "Renderizando ${filteredUsers.size} usuarios")
                        items(filteredUsers) { user ->
                            Log.d("LAZY_COLUMN", "Renderizando item para usuario: ${user.firstName}")
                            UserCard(
                                user = user,
                                onClick = {
                                    selectedUser = user
                                    showEditUserModal = true
                                },
                                onDelete = { userToDelete ->
                                    scope.launch {
                                        try {
                                            Log.d("DELETE_USER", "Eliminando usuario ${userToDelete.id}: ${userToDelete.firstName}")
                                            val response = api.deleteUser("Bearer $accessToken", userToDelete.id)

                                            if (response.isSuccessful) {
                                                Log.d("DELETE_USER", "Usuario eliminado exitosamente")
                                                
                                                // Recargar la lista de usuarios
                                                try {
                                                    val usersResponse = api.getUsers("Bearer $accessToken")
                                                    if (usersResponse.isSuccessful) {
                                                        allUsers = usersResponse.body() ?: emptyList()
                                                        Log.d("DELETE_USER", "Lista de usuarios actualizada: ${allUsers.size}")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("DELETE_USER", "Error recargando usuarios: ${e.message}")
                                                }
                                            } else {
                                                Log.e("DELETE_USER", "Error eliminando usuario: ${response.code()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("DELETE_USER", "Excepción eliminando usuario: ${e.message}", e)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

        }

        // FloatingActionButton para crear usuario
        FloatingActionButton(
            onClick = { showCreateUserModal = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp, 0.dp, 16.dp, 150.dp), // Padding inferior para evitar solapamiento con navegación
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear usuario",
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
                    isSelected = false,
                    onClick = onNavigateToBooks
                )
                NavigationButton(
                    icon = Icons.Default.Person,
                    label = "Usuarios",
                    isSelected = true,
                    onClick = { /* Ya estamos aquí */ }
                )
            }
        }
    }

        // Modal para crear usuario
        if (showCreateUserModal) {
        CreateUserModal(
            onDismiss = { showCreateUserModal = false },
            onCreateUser = { userRequest, onError ->
              scope.launch {
                  try {
                      Log.d("CREATE_USER", "Creando usuario: $userRequest")
          
                      val response = api.createUser("Bearer $accessToken", userRequest)
          
                      if (response.isSuccessful) {
                          Log.d("CREATE_USER", "Usuario creado exitosamente: ${response.body()}")
          
                          // Cerrar modal de creación
                          showCreateUserModal = false
          
                          // Recargar lista de usuarios
                          try {
                              val usersResponse = api.getUsers("Bearer $accessToken")
                              if (usersResponse.isSuccessful) {
                                  allUsers = usersResponse.body() ?: emptyList()
                                  Log.d("CREATE_USER", "Lista de usuarios actualizada: ${allUsers.size}")
                              } else {
                                  Log.e("CREATE_USER", "Error al recargar usuarios: ${usersResponse.code()}")
                                  onError("No se pudo recargar la lista de usuarios.")
                              }
                          } catch (e: Exception) {
                              Log.e("CREATE_USER", "Excepción recargando usuarios: ${e.message}", e)
                              onError("Error recargando usuarios: ${e.message}")
                          }
          
                      } else {
                          Log.e("CREATE_USER", "Error creando usuario: ${response.code()}")
          
                          // Intentar extraer mensaje desde el cuerpo del error
                          val errorMessage = try {
                              val rawError = response.errorBody()?.string()
                              Log.e("CREATE_USER", "Error body: $rawError")
          
                              if (!rawError.isNullOrEmpty()) {
                                  val json = JSONObject(rawError)
                                  json.optString(
                                      "message",
                                      "Ocurrió un error al crear el usuario. Por favor, intenta nuevamente."
                                  )
                              } else {
                                  "Error desconocido al crear el usuario."
                              }
                          } catch (e: Exception) {
                              Log.e("CREATE_USER", "Error procesando errorBody: ${e.message}")
                              "Ocurrió un error inesperado al procesar la respuesta."
                          }
          
                          // Mostrar mensaje al usuario
                          onError(errorMessage)
                      }
          
                  } catch (e: Exception) {
                      Log.e("CREATE_USER", "Excepción creando usuario: ${e.message}", e)
                      onError("Error de conexión: ${e.message}")
                  }
              }
            } 
        )
    }

        // Modal para editar usuario
        if (showEditUserModal && selectedUser != null) {
            EditUserModal(
                user = selectedUser!!,
                onDismiss = { 
                    showEditUserModal = false
                    selectedUser = null
                },
                onUpdateUser = { userId, userRequest, onError ->
                  scope.launch {
                      try {
                          Log.d("UPDATE_USER", "Actualizando usuario $userId: $userRequest")
              
                          val response = api.updateUser("Bearer $accessToken", userId, userRequest)
              
                          if (response.isSuccessful) {
                              Log.d("UPDATE_USER", "Usuario actualizado exitosamente: ${response.body()}")
              
                              // Cerrar el modal y limpiar selección
                              showEditUserModal = false
                              selectedUser = null
              
                              // Recargar lista de usuarios
                              try {
                                  val usersResponse = api.getUsers("Bearer $accessToken")
                                  if (usersResponse.isSuccessful) {
                                      allUsers = usersResponse.body() ?: emptyList()
                                      Log.d("UPDATE_USER", "Lista de usuarios actualizada: ${allUsers.size}")
                                  } else {
                                      Log.e("UPDATE_USER", "Error al recargar usuarios: ${usersResponse.code()}")
                                      onError("No se pudo recargar la lista de usuarios.")
                                  }
                              } catch (e: Exception) {
                                  Log.e("UPDATE_USER", "Excepción recargando usuarios: ${e.message}", e)
                                  onError("Error recargando usuarios: ${e.message}")
                              }
              
                          } else {
                              Log.e("UPDATE_USER", "Error actualizando usuario: ${response.code()}")
              
                              // Intentar obtener mensaje desde el cuerpo del error
                              val errorMessage = try {
                                  val rawError = response.errorBody()?.string()
                                  Log.e("UPDATE_USER", "Error body: $rawError")
              
                                  if (!rawError.isNullOrEmpty()) {
                                      val json = JSONObject(rawError)
                                      json.optString("message", "Error desconocido al actualizar el usuario.")
                                  } else {
                                      "Error desconocido al actualizar el usuario."
                                  }
                              } catch (e: Exception) {
                                  Log.e("UPDATE_USER", "Error procesando errorBody: ${e.message}")
                                  "Ocurrió un error inesperado al procesar la respuesta."
                              }
              
                              // Mostrar mensaje al usuario
                              onError(errorMessage)
                          }
              
                      } catch (e: Exception) {
                          Log.e("UPDATE_USER", "Excepción actualizando usuario: ${e.message}", e)
                          onError("Error de conexión: ${e.message}")
                      }
                  }
              }              
            )
        }
}

@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
    onDelete: (User) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Log.d("USER_CARD", "Renderizando UserCard para: ${user.firstName}")
    
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
            // Nombre completo y botón eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${user.firstName} ${user.secondName} ${user.lastName} ${user.secondLastName}",
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
                    // Indicador de rol
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (user.role) {
                                "bibliotecario" -> MaterialTheme.colorScheme.primaryContainer
                                "estudiante" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.tertiaryContainer
                            }
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Text(
                        text = when (user.role) {
                            "bibliotecario" -> "📚 Bibliotecario"
                            "estudiante" -> "👤 Estudiante"
                            else -> "❓ ${user.role}"
                        },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        color = when (user.role) {
                            "bibliotecario" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "estudiante" -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
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
                            contentDescription = "Eliminar usuario",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Email",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cédula
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Cédula",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = user.cedula,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "Eliminar usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres eliminar al usuario \"${user.firstName} ${user.lastName}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(user)
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
