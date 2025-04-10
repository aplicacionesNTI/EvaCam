package com.itevebasa.evacam.modelos

data class UploadRequest(
    val inspeccion: String,
    val anyo: String,
    val files: Map<String, String>
)
