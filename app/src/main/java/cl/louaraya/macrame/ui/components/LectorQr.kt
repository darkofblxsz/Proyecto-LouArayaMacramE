package cl.louaraya.macrame.ui.components

import cl.louaraya.macrame.R
import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.viewmodel.*
import cl.louaraya.macrame.ui.screens.*

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

// Solo reconoce códigos del catálogo; no abre enlaces recibidos por QR.
fun codigoDesdeQr(texto: String): String? {
    val limpio = texto.trim().removePrefix("LOUARAYA:").uppercase(java.util.Locale.ROOT)
    return limpio.takeIf { it.matches(Regex("MAC-[0-9]{3,10}")) }
}

@Composable
fun BotonLectorQr(onResultado: (String) -> Unit) {
    val context = LocalContext.current
    val resultadoActual by rememberUpdatedState(onResultado)
    val lector = rememberLauncherForActivityResult(ScanContract()) { resultado ->
        resultado.contents?.let { resultadoActual(it) }
    }
    fun abrir() {
        try {
            lector.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Apunta al QR de un producto LouAraya")
                .setBeepEnabled(false).setOrientationLocked(false))
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir la cámara", Toast.LENGTH_LONG).show()
        }
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) abrir() else Toast.makeText(context, "Permite la cámara en Ajustes para leer QR", Toast.LENGTH_LONG).show()
    }
    OutlinedButton(onClick = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrir()
        else permiso.launch(Manifest.permission.CAMERA)
    }) { Text("Escanear QR") }
}

@Composable
fun DialogoQr(producto: ProductoLocal, onCerrar: () -> Unit) {
    val bitmap = remember(producto.codigo) {
        BarcodeEncoder().encodeBitmap("LOUARAYA:${producto.codigo}", BarcodeFormat.QR_CODE, 512, 512).asImageBitmap()
    }
    AlertDialog(onDismissRequest = onCerrar, title = { Text(producto.nombre) }, text = {
        Column {
            Image(bitmap, "QR de ${producto.codigo}", Modifier.fillMaxWidth().height(230.dp))
            Text(producto.codigo)
            Text("Escanea este código desde otro dispositivo con el mismo catálogo.")
        }
    }, confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } })
}
