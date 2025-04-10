package com.itevebasa.evacam.modelos

import com.google.gson.annotations.SerializedName

class ApiResponse(
    val limit: Int,
    val start: Int,
    @SerializedName("objects") val objects: List<Item>
){
    override fun toString(): String {
        return "ApiResponse(limit=$limit, start=$start, objects=$objects)"
    }
}
