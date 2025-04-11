package com.itevebasa.evacam.actividades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itevebasa.evacam.modelos.Item
import com.itevebasa.evacam.modelos.Photos
import com.itevebasa.evacam.R
import com.itevebasa.evacam.conexion.RetrofitClient
import com.itevebasa.evacam.modelos.UploadRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import com.itevebasa.evacam.auxiliar.Vistas
import com.itevebasa.evacam.auxiliar.Imagenes
import com.itevebasa.evacam.auxiliar.Localizacion
import com.itevebasa.evacam.auxiliar.Permisos
import com.itevebasa.evacam.auxiliar.VariablesGlobales
import com.itevebasa.evacam.auxiliar.Vistas.Companion.dpToPx
import kotlinx.coroutines.launch

class DetallesActivity : AppCompatActivity() {

    private val extraImageViews = mutableListOf<ImageView>()
    private var location: Location? = null
    private var selectedImageView: ImageView? = null
    private lateinit var item: Item
    private var photoUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission") //Se comprueban los permisos anteriormente
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalles)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val progressDialog = Vistas.showWaitDialog(this)
        lifecycleScope.launch {
            location = Localizacion.getCurrentLocation(this@DetallesActivity)
            progressDialog.dismiss()
        }
        item = Item(
            intent.getStringExtra("seccion_id")!!,
            intent.getStringExtra("codigo")!!,
            intent.getStringExtra("anyo")!!,
            intent.getStringExtra("lineainspeccion")!!,
            intent.getStringExtra("codvehiculo")!!
        )

        val bastidorTextView: TextView = findViewById(R.id.textView)
        val bastidorImgView: ImageView = findViewById(R.id.imageView)
        val bastidorCardView: CardView = findViewById(R.id.cardView)
        val cuentakmTextView: TextView = findViewById(R.id.textView2)
        val cuentakmImgView: ImageView = findViewById(R.id.imageView2)
        val cuentakmCardView: CardView = findViewById(R.id.cardView2)
        val frontalImgView: ImageView = findViewById(R.id.imageView3)
        val traseraTextView: TextView = findViewById(R.id.textView4)
        val traseraImgView: ImageView = findViewById(R.id.imageView4)
        val traseraCardView: CardView = findViewById(R.id.cardView4)
        val contenedor: LinearLayout = findViewById(R.id.linearLayout)
        val contenedorAdd: LinearLayout = findViewById(R.id.addLayout)
        val enviarButton: Button = findViewById(R.id.enviarButton)
        val addTextView: TextView = findViewById(R.id.textView5)
        val addButton = findViewById<Button>(R.id.agregarFotoButton)
        if (VariablesGlobales.tipoInforme == "Solo informe"){
            contenedor.removeView(bastidorTextView)
            contenedor.removeView(bastidorCardView)
            contenedor.removeView(cuentakmTextView)
            contenedor.removeView(cuentakmCardView)
            contenedor.removeView(traseraTextView)
            contenedor.removeView(traseraCardView)
            contenedor.removeView(addTextView)
            contenedorAdd.removeView(addButton)
        }else if(VariablesGlobales.tipoInforme == "Informe sin extras"){
            contenedor.removeView(addTextView)
            contenedorAdd.removeView(addButton)
        }
        addButton.setOnClickListener {
            if (VariablesGlobales.cantFotosExtra < 7){
                addImageCard(this, contenedor)
            }else{
                Toast.makeText(this, "Has alcanzado la cantidad máxima de fotos extra", Toast.LENGTH_SHORT).show()
            }

        }
        bastidorImgView.setOnClickListener { openImagePreview(bastidorImgView) }
        cuentakmImgView.setOnClickListener { openImagePreview(cuentakmImgView) }
        frontalImgView.setOnClickListener { openImagePreview(frontalImgView) }
        traseraImgView.setOnClickListener { openImagePreview(traseraImgView) }
        enviarButton.setOnClickListener {
            if ((VariablesGlobales.tipoInforme == "Solo informe" && frontalImgView.drawable == null) ||
                ((VariablesGlobales.tipoInforme == "Informe sin extras" || VariablesGlobales.tipoInforme == "Todas las fotos") &&
                bastidorImgView.drawable == null || cuentakmImgView.drawable == null || frontalImgView.drawable == null || traseraImgView.drawable == null)){
                Toast.makeText(this@DetallesActivity, "Realiza las fotos obligatorias antes de enviar", Toast.LENGTH_LONG).show()
            }else{
                val extraImages = mutableListOf<String>()
                extraImageViews.forEach { imageView ->
                    val encodedImage =
                        Imagenes.encodeImageViewUriToBase64(imageView, this)
                    if (!encodedImage.isNullOrBlank()) {
                        extraImages.add(encodedImage)
                    }
                }
                val photos = Photos(
                    bastidorBase64 = Imagenes.encodeImageViewUriToBase64(bastidorImgView, this) ?: "",
                    cuentaKmBase64 = Imagenes.encodeImageViewUriToBase64(cuentakmImgView, this) ?: "",
                    frontalBase64 = Imagenes.encodeImageViewUriToBase64(frontalImgView, this) ?: "",
                    traseraBase64 = Imagenes.encodeImageViewUriToBase64(traseraImgView, this) ?: "",
                    extras = extraImages
                )
                uploadPhotos(this, photos)
            }
        }
    }

    // Definir el ActivityResultLauncher para capturar la imagen
    @SuppressLint("MissingPermission") //Se comprueban los permisos anteriormente
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val takePictureLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val photo: Bitmap? = Imagenes.getBitmapFromUri(this@DetallesActivity, photoUri)
                // Verificar si se obtuvo la ubicación
                if (location == null) {
                    Toast.makeText(
                        this@DetallesActivity,
                        "No se pudo obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("DetallesActivity", "Ubicación no disponible")
                } else {
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                    if (photo != null) {
                        val reducedPhoto: Bitmap = if (photo.width>photo.height){
                            photo.scale(1024, 720)
                        }else{
                            photo.scale(720, 960)
                        }

                        // Aplicar marca de agua
                        val imageWithWatermark = Imagenes.mark(
                            reducedPhoto,
                            LocalDateTime.now().format(formatter).toString()
                        )

                        // Guardar la imagen con metadatos EXIF (solo si hay ubicación)
                        photoUri = Imagenes.guardarImagenConMetadatos(
                            this@DetallesActivity,
                            imageWithWatermark,
                            location
                        )
                        selectedImageView?.setImageURI(photoUri)
                        selectedImageView?.tag = photoUri
                    } else {
                        Log.e("DetallesActivity", "No se pudo decodificar la imagen desde la URI.")
                    }
                }
            }else {
                Toast.makeText(this, "Foto no tomada", Toast.LENGTH_SHORT).show()
            }
        }

    //Versión para fotos obligatorias
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openImagePreview(imgView: ImageView) {
        selectedImageView = imgView
        if (imgView.drawable == null) {
            abrirCamaraConPermisos()
        } else {
            Vistas.showImagePreview(this, imgView)
        }
    }

    //Versión para fotos extra
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openImagePreview(cardView: CardView, container: LinearLayout, imgView: ImageView) {
        selectedImageView = imgView
        if (imgView.drawable == null) {
            abrirCamaraConPermisos()
        } else {
            Vistas.showImagePreview(this, container, cardView, imgView, extraImageViews)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun abrirCamaraConPermisos(){
        // Verificar si tenemos permiso para acceder a la cámara
        if (Permisos.isCameraPermissionGranted(this) && Permisos.isStoragePermissionGranted(this) && Permisos.isLocationPermissionGranted(this)) {
            openCamera()
        } else {
            Permisos.requestPermissions(this)
        }
    }

    // Función para abrir la cámara
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openCamera() {
        val photoFile = Imagenes.createImageFile(this)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri) // Guardar la foto en el archivo
        }
        takePictureLauncher.launch(intent)
    }

    private fun obtenerFotos(photos: Photos): MutableMap<String, String> {
        if (VariablesGlobales.tipoInforme == "Solo informe"){
            val filesMap = mutableMapOf(
                "fotoInforme" to photos.traseraBase64
            )
            return filesMap
        }else{
            val filesMap = mutableMapOf(
                "bastidor" to photos.bastidorBase64,
                "cuentakm" to photos.cuentaKmBase64,
                "matriculaEsquina1" to photos.frontalBase64,
                "matriculaEsquina2" to photos.traseraBase64,
                "fotoInforme" to photos.traseraBase64
            )
            // Agregar imágenes extra
            photos.extras.forEachIndexed { index, base64Image ->
                filesMap["extra_${index + 1}"] = base64Image
            }
            return filesMap
        }
    }

    // Función para enviar las fotos al servidor
    private fun uploadPhotos(context: Context, photos: Photos) {
        val progressDialog = Vistas.showProgressDialog(context)
        val filesMap = obtenerFotos(photos)
        val requestBody = UploadRequest(
            inspeccion = item.codigo!!,
            anyo = item.anyo!!,
            files = filesMap
        )
        val apiService = RetrofitClient.getApiService(VariablesGlobales.conexion)
        val call = apiService.uploadPhotos(requestBody)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Log.d("CANTIDAD_FILES", " " + filesMap.size)
                    Toast.makeText(context, "Imágenes enviadas correctamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Error al enviar imágenes", Toast.LENGTH_SHORT).show()
                    Log.d("API ERROR", "Headers: ${response.headers()}")
                    Log.d("API ERROR", "Mensaje: ${response.message()}")
                    Log.d("API", "ERROR enviar imágenes ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismiss()
                Log.d("API", "ERROR LLAMADA API: ${t.message}")
            }
        })
    }

    // Metodo que se llama cuando se recibe la respuesta del usuario sobre los permisos
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Permisos.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso denegado para usar la cámara o escribir en almacenamiento", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función para agregar un nuevo CardView con ImageView interactivo
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun addImageCard(context: Context, container: LinearLayout) {
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dpToPx(context,4), dpToPx(context,8), dpToPx(context,4), dpToPx(context,8))
            }
            var radiusInDp = 16
            val scale = resources.displayMetrics.density
            var radiusInPx = radiusInDp * scale
            radius = radiusInPx
            radiusInDp = 6
            radiusInPx = radiusInDp * scale
            cardElevation = radiusInPx
            preventCornerOverlap = true
            useCompatPadding = true
        }

        val imageView = ImageView(this).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(context,250) // Convertir 250dp a píxeles
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setOnClickListener {
                openImagePreview(cardView, container, this)
            }
        }
        // Agrega el nuevo ImageView a la lista para referencia futura
        extraImageViews.add(imageView)
        cardView.addView(imageView)
        container.addView(cardView)
        VariablesGlobales.cantFotosExtra++
    }
}