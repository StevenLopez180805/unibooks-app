package com.dowers.unibooks.data.models

data class Book(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val escritor: String,
    val ubicacion: String,
    val stock: Int
)
