package com.itevebasa.evacam.modelos

data class Photos(
    val bastidorBase64: String,
    val cuentaKmBase64: String,
    val frontalBase64: String,
    val traseraBase64: String,
    val extras: List<String> = emptyList()
)
