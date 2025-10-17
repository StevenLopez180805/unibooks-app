package com.dowers.unibooks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dowers.unibooks.data.models.User
import com.dowers.unibooks.data.remote.CreateUserRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserModal(
    user: User,
    onDismiss: () -> Unit,
    onUpdateUser: (Int, CreateUserRequest, (String?) -> Unit) -> Unit
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var secondName by remember { mutableStateOf(user.secondName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var secondLastName by remember { mutableStateOf(user.secondLastName) }
    var cedula by remember { mutableStateOf(user.cedula) }
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf("") } // Campo vacío para nueva contraseña
    var role by remember { mutableStateOf(user.role) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    .padding(24.dp)
            ) {
                // Área scrolleable para los campos
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Título del modal
                Text(
                    text = "✏️ Editar Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Campo primer nombre
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Primer nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo segundo nombre
                OutlinedTextField(
                    value = secondName,
                    onValueChange = { secondName = it },
                    label = { Text("Segundo nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo primer apellido
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Primer apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo segundo apellido
                OutlinedTextField(
                    value = secondLastName,
                    onValueChange = { secondLastName = it },
                    label = { Text("Segundo apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo cédula
                OutlinedTextField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = { Text("Cédula") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                // Campo email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                // Campo contraseña (opcional para edición)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nueva contraseña (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    placeholder = { Text("Dejar vacío para mantener la actual") }
                )

                // Campo rol
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Estudiante") },
                            onClick = {
                                role = "estudiante"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bibliotecario") },
                            onClick = {
                                role = "bibliotecario"
                                expanded = false
                            }
                        )
                    }
                }
                
                // Mensaje de error
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage!!,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                }

                // Botones (fuera del scroll)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Button(
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            val userRequest = CreateUserRequest(
                                firstName = firstName,
                                secondName = secondName,
                                lastName = lastName,
                                secondLastName = secondLastName,
                                cedula = cedula,
                                email = email,
                                password = password.ifEmpty { user.password }, // Mantener contraseña actual si no se especifica nueva
                                role = role
                            )
                            onUpdateUser(user.id, userRequest) { error ->
                                errorMessage = error
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = firstName.isNotEmpty() && lastName.isNotEmpty() && 
                                cedula.isNotEmpty() && email.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Actualizar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
