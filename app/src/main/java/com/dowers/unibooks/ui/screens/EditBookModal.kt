package com.dowers.unibooks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dowers.unibooks.data.models.Book
import com.dowers.unibooks.data.remote.CreateBookRequest

@Composable
fun EditBookModal(
    book: Book,
    onDismiss: () -> Unit,
    onUpdateBook: (Int, CreateBookRequest) -> Unit
) {
    var titulo by remember { mutableStateOf(book.titulo) }
    var escritor by remember { mutableStateOf(book.escritor) }
    var descripcion by remember { mutableStateOf(book.descripcion) }
    var ubicacion by remember { mutableStateOf(book.ubicacion) }
    var stock by remember { mutableStateOf(book.stock.toString()) }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título del modal
                Text(
                    text = "✏️ Editar Libro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Campo título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del libro") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo escritor
                OutlinedTextField(
                    value = escritor,
                    onValueChange = { escritor = it },
                    label = { Text("Escritor") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                // Campo ubicación
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Campo stock
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            val stockInt = stock.toIntOrNull() ?: 0
                            val bookRequest = CreateBookRequest(
                                titulo = titulo,
                                escritor = escritor,
                                descripcion = descripcion,
                                ubicacion = ubicacion,
                                stock = stockInt
                            )
                            onUpdateBook(book.id, bookRequest)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = titulo.isNotEmpty() && escritor.isNotEmpty() && ubicacion.isNotEmpty() && stock.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Actualizar")
                        }
                    }
                }
            }
        }
    }
}
