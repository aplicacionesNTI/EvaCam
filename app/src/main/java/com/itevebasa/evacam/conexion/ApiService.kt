package com.itevebasa.evacam.conexion

import com.itevebasa.evacam.modelos.ApiResponse
import com.itevebasa.evacam.modelos.UploadRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @Headers("Accept: application/json")
    @GET("nti_insp_iniciada")
    fun getItems(@Query("fields") fields: String): Call<ApiResponse>
    @Headers("Content-Type: application/json")
    @PUT("ws_fotos")
    fun uploadPhotos(@Body requestBody: UploadRequest): Call<ResponseBody>
}
