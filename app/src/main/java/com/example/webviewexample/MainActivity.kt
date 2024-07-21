package com.example.webviewexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.webviewexample.data.HTTPS_SITE
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {
    private lateinit var myWebView: WebView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    inner class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun getLocation(): String {
            // Llamar a la función para obtener la ubicación
            requestLocationUpdates()
            return "$latitude,$longitude"
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializa WebView
        myWebView = findViewById(R.id.myWebView)
        val webSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.setGeolocationEnabled(true)
        webSettings.domStorageEnabled = true

        // Agrega la interfaz de JavaScript
        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")

        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                } else {
                    callback.invoke(origin, true, false)
                }
            }
        }

        myWebView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showErrorDialog("Error de carga", "No se pudo cargar la página.")
            }
        }

        myWebView.loadUrl(HTTPS_SITE.url)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates()
        } else {
            showPermissionDeniedDialog()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permiso de ubicación necesario")
        builder.setMessage("Esta aplicación no funcionará correctamente sin acceso a la ubicación. Por favor, conceda los permisos de ubicación en la configuración.")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.setNegativeButton("Salir") { dialog, _ ->
            dialog.dismiss()
            finish() // Cierra la aplicación si el usuario elige salir
        }
        builder.create().show()
    }

    private fun showErrorDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                30000
            ).apply {
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (location in locationResult.locations) {
                        latitude = location.latitude
                        longitude = location.longitude
                        val locationString = "$latitude,$longitude"
                        // Envía la ubicación al WebView mediante JavaScript
                        myWebView.post {
                            myWebView.evaluateJavascript("updateLocation('$locationString')", null)
                        }
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Si no se tiene permiso, muestra un mensaje de error
            myWebView.post {
                myWebView.evaluateJavascript("updateLocation('Permiso de ubicación no concedido')", null)
            }
        }
    }
}
