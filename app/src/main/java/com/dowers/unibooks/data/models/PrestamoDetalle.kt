package com.dowers.unibooks.data.models

data class PrestamoDetalle(
    val id: String,
    val libro: String,
    val estudiante: String,
    val fechaPrestamo: String,
    val fechaDevolucion: String,
    val fechaLimite: String,
    val estado: String,
    val diasRestantes: Int
)
