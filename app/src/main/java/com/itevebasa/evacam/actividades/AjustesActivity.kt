package com.itevebasa.evacam.actividades

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.itevebasa.evacam.R
import com.itevebasa.evacam.auxiliar.VariablesGlobales


class AjustesActivity : AppCompatActivity() {
    private lateinit var confirmarButton : Button
    private lateinit var conexion: EditText
    private lateinit var usuario: EditText
    private lateinit var password: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ajustes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkBox = findViewById(R.id.checkBox)
        confirmarButton = findViewById(R.id.button)
        conexion = findViewById(R.id.conexionEditText)
        usuario = findViewById(R.id.usuarioEditText)
        password = findViewById(R.id.passwordEditText)
        radioGroup = findViewById(R.id.radioGroup)

        val prefs = getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE)
        val guardarSeleccion = prefs.getBoolean("guardarSeleccion", false)
        if (guardarSeleccion){
            conexion.setText(prefs.getString("conexion", ""))
            usuario.setText(prefs.getString("usuario", ""))
            password.setText(prefs.getString("password", ""))
            seleccionarRadioPorTexto(radioGroup, prefs.getString("tipo_informe", "")!!)
            checkBox.isChecked = true
        }
        confirmarButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadioButton = findViewById<RadioButton>(selectedId)
                val seleccionTexto = selectedRadioButton.text.toString()
                VariablesGlobales.conexion = conexion.text.toString()
                VariablesGlobales.usuario = usuario.text.toString()
                VariablesGlobales.password = password.text.toString()
                VariablesGlobales.tipoInforme = seleccionTexto
                if (checkBox.isChecked){

                    with(prefs.edit()) {
                        putBoolean("guardarSeleccion", true)
                        putString("conexion", VariablesGlobales.conexion)
                        putString("usuario", VariablesGlobales.usuario)
                        putString("password", VariablesGlobales.password)
                        putString("tipo_informe", VariablesGlobales.tipoInforme)
                        apply()
                    }
                }else{
                    with(prefs.edit()) {
                        putBoolean("guardarSeleccion", false)
                        remove("conexion")
                        remove("usuario")
                        remove("password")
                        remove("tipo_informe")
                        apply()
                    }
                }
                if (VariablesGlobales.conexion == "" || VariablesGlobales.usuario == "" || VariablesGlobales.password == ""){
                    Toast.makeText(this, "La conexión, usuario y contraseña no peuden estar vacios", Toast.LENGTH_SHORT).show()
                }else{
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Selecciona una opción", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun seleccionarRadioPorTexto(radioGroup: RadioGroup, texto: String) {
        for (i in 0 until radioGroup.childCount) {
            val child = radioGroup.getChildAt(i)
            if (child is RadioButton) {
                if (child.text.toString() == texto) {
                    radioGroup.check(child.id)
                    break
                }
            }
        }
    }

}
