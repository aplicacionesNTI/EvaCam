package com.itevebasa.evacam.actividades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.itevebasa.evacam.modelos.Item
import com.itevebasa.evacam.R
import com.itevebasa.evacam.auxiliar.Imagenes
import com.itevebasa.evacam.auxiliar.Permisos
import com.itevebasa.evacam.auxiliar.VariablesGlobales
import com.itevebasa.evacam.conexion.RetrofitClient
import com.itevebasa.evacam.modelos.ApiResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var textoEstacion: TextView
    private var itemList: MutableList<Item> = mutableListOf()
    private lateinit var ajustes: ImageButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!Permisos.isCameraPermissionGranted(this) || !Permisos.isStoragePermissionGranted(this) || !Permisos.isLocationPermissionGranted(this)) {
            Permisos.requestPermissions(this)
        }
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Recoge las preferencias guardadas localmente
        val prefs = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE)

        if (prefs.getBoolean("guardarSeleccion", false)){
            VariablesGlobales.estacion = prefs.getString("estacion", "")!!
            VariablesGlobales.conexion = prefs.getString("conexion", "")!!
            VariablesGlobales.usuario = prefs.getString("usuario", "")!!
            VariablesGlobales.password = prefs.getString("password", "")!!
            VariablesGlobales.tipoInforme = prefs.getString("tipo_informe", "")!!
        }
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }

        recyclerView = findViewById(R.id.recyclerview)
        ajustes = findViewById(R.id.settings_button)
        textoEstacion = findViewById(R.id.textEstacion)
        textoEstacion.text = "Estación: " + VariablesGlobales.conexion
        ajustes.setOnClickListener{
            val intent = Intent(this, AjustesActivity::class.java)
            startActivity(intent)
        }
        // Configura el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(itemList) { item ->
            val intent = Intent(this, DetallesActivity::class.java).apply {
                putExtra("seccion_id", item.seccion_id)
                putExtra("codigo", item.codigo)
                putExtra("anyo", item.anyo)
                putExtra("lineainspeccion", item.lineainspeccion)
                putExtra("codvehiculo", item.codvehiculo)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        if (VariablesGlobales.conexion != ""){
            fetchData()
        }else{
            Toast.makeText(this@MainActivity, "Entra en ajustes y selecciona una estación", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()
        // Eliminar los archivos del directorio "MyAppImages" cuando se inicia la actividad
        val dir = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!, "MyAppImages")
        Imagenes.borrarContenidoCarpeta(dir)
    }

    private fun fetchData() {
        swipeRefreshLayout.isRefreshing = true
        val apiService = RetrofitClient.getApiService(VariablesGlobales.conexion)
        apiService.getItems("*").enqueue(object : Callback<ApiResponse> {
            @SuppressLint("NotifyDataSetChanged")
            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                swipeRefreshLayout.isRefreshing = false
                Log.d("API", "Accediendo llamada a la API")
                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("API", "Cuerpo de la respuesta: ${response.body()}")
                        Log.d("API", "" + it.toString())
                        itemList.clear()
                        itemList.addAll(it.objects)  // Accede a 'objects' que contiene el array de items
                        Log.d("OBJECTS",  " " + it.objects )
                        adapter.notifyDataSetChanged() // Notifica al adaptador que los datos han cambiado
                    }
                } else {
                    Log.d("API", "ERROR: " + response.code())
                    Toast.makeText(this@MainActivity, "Error: ${JSONObject(response.errorBody()?.string()).getString("description")}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Log.d("API", "Fallo llamada a la API: " + t.message)
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}