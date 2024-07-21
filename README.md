# WebView Example Application

Esta es una aplicación Android que carga una aplicación web Angular en un WebView y obtiene la ubicación del dispositivo para enviarla a la aplicación web.

## Configuración

### Clonar el Repositorio

```sh
git clone https://github.com/DaveDeveloper117/webview-example.git
cd webview-example
``` 

### Abrir en Android Studio

1.  Abre Android Studio.
2.  Selecciona "Open an existing Android Studio project".
3.  Navega a la carpeta donde clonaste el repositorio y selecciona `webview-example`.

### Cambiar el Endpoint

Para utilizar un endpoint diferente, debes cambiar la URL en los siguientes archivos:

#### `com.example.webviewexample.data.HTTPS_SITE`


```kotlin
package com.example.webviewexample.data

data class Endpoints(
    val url: String
)

val HTTPS_SITE = Endpoints(
    url = "http://TU_NUEVO_ENDPOINT/"
)
``` 

#### `res/xml/network_security_config.xml`


```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">TU_NUEVO_ENDPOINT</domain>
    </domain-config>
</network-security-config>
``` 

### Permisos

Asegúrate de que los siguientes permisos están presentes en el archivo `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 ``` 

### Ejecutar la Aplicación

1.  Conecta tu dispositivo Android o inicia un emulador.
2.  En Android Studio, haz clic en el botón de "Run" o selecciona `Run > Run 'app'`.
3.  La aplicación debería instalarse y ejecutarse en tu dispositivo o emulador.
