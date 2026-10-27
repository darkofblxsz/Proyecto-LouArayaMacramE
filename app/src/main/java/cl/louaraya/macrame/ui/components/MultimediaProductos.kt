package cl.louaraya.macrame.ui.components

import cl.louaraya.macrame.R
import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.viewmodel.*
import cl.louaraya.macrame.ui.screens.*

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Copia privada: las fotos no dependen del permiso temporal de la galería.
private fun importarFoto(context: Context, uri: Uri): String {
    val carpeta = File(context.filesDir, "productos").apply { mkdirs() }
    val destino = File(carpeta, "${UUID.randomUUID()}.jpg")
    try {
        context.contentResolver.openInputStream(uri).use { entrada ->
            requireNotNull(entrada) { "No se pudo abrir la imagen" }
            destino.outputStream().use { salida ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val n = entrada.read(buffer)
                    if (n < 0) break
                    total += n
                    require(total <= 20 * 1024 * 1024) { "Cada foto debe pesar menos de 20 MB" }
                    salida.write(buffer, 0, n)
                }
            }
        }
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(destino.path, opciones)
        require(opciones.outWidth > 0 && opciones.outHeight > 0) { "El archivo no es una imagen válida" }
        return destino.name
    } catch (e: Exception) {
        destino.delete()
        throw e
    }
}

@Composable
fun FotoProducto(foto: String?, categoria: String, descripcion: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val recurso = when (categoria.lowercase()) {
        "matrimonios" -> R.drawable.matrimonios
        "festivales" -> R.drawable.festivales
        "viajes" -> R.drawable.viajes
        else -> R.drawable.hogar
    }
    AsyncImage(
        model = foto?.let { File(context.filesDir, "productos/$it") },
        contentDescription = descripcion,
        placeholder = painterResource(recurso), error = painterResource(recurso),
        fallback = painterResource(recurso), contentScale = ContentScale.Crop, modifier = modifier
    )
}

@Composable
fun EditorFotos(fotos: List<String>, categoria: String, onCambiar: (List<String>) -> Unit,
                onOcupado: (Boolean) -> Unit, maxFotos: Int = 5, titulo: String = "Fotografías") {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val actuales by rememberUpdatedState(fotos)
    val cambiar by rememberUpdatedState(onCambiar)
    val ocupadoCallback by rememberUpdatedState(onOcupado)
    var ocupado by remember { mutableStateOf(false) }
    var pendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf("") }

    fun importar(uris: List<Uri>) {
        if (uris.isEmpty()) return
        ocupado = true
        ocupadoCallback(true)
        scope.launch {
            try {
                val nuevas = mutableListOf<String>()
                var fallos = 0
                withContext(Dispatchers.IO) {
                    uris.take((maxFotos - actuales.size).coerceAtLeast(0)).forEach { uri ->
                        try { nuevas.add(importarFoto(context, uri)) } catch (_: Exception) { fallos++ }
                    }
                }
                cambiar(actuales + nuevas)
                error = if (fallos > 0) "No se pudieron cargar $fallos fotos. Usa imágenes de menos de 20 MB." else ""
            } finally {
                ocupado = false
                ocupadoCallback(false)
            }
        }
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { importar(it) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val ruta = pendiente
        pendiente = null
        if (ok && ruta != null) {
            val archivo = File(ruta)
            ocupado = true
            ocupadoCallback(true)
            scope.launch {
                try {
                    val nombre = withContext(Dispatchers.IO) {
                        importarFoto(context, FileProvider.getUriForFile(context, "${context.packageName}.files", archivo))
                    }
                    cambiar((actuales + nombre).take(maxFotos))
                    error = ""
                } catch (_: Exception) { error = "No se pudo guardar la fotografía. Intenta nuevamente." }
                finally { archivo.delete(); ocupado = false; ocupadoCallback(false) }
            }
        } else ruta?.let { File(it).delete() }
    }
    fun abrirCamara() {
        try {
            val carpeta = File(context.cacheDir, "camera").apply { mkdirs() }
            val archivo = File.createTempFile("foto_", ".jpg", carpeta)
            pendiente = archivo.path
            camara.launch(FileProvider.getUriForFile(context, "${context.packageName}.files", archivo))
        } catch (_: Exception) {
            pendiente?.let { File(it).delete() }; pendiente = null
            error = "No hay una aplicación de cámara disponible. Puedes elegir una foto de la galería."
        }
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else error = "Permiso de cámara denegado. Puedes usar la galería o habilitarlo en Ajustes."
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$titulo (${fotos.size}/$maxFotos)", style = MaterialTheme.typography.titleSmall)
        if (maxFotos > 1) Text("La primera foto será la portada.", style = MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fotos, key = { it }) { foto ->
                Column(Modifier.width(100.dp)) {
                    FotoProducto(foto, categoria, "Foto del producto", Modifier.size(100.dp))
                    TextButton(enabled = !ocupado, onClick = { cambiar(actuales - foto) }) { Text("Quitar") }
                    if (foto != fotos.firstOrNull()) {
                        TextButton(enabled = !ocupado, onClick = { cambiar(listOf(foto) + (actuales - foto)) }) { Text("Portada") }
                    }
                }
            }
        }
        OutlinedButton(enabled = !ocupado && fotos.size < maxFotos, onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrirCamara()
            else permiso.launch(Manifest.permission.CAMERA)
        }, modifier = Modifier.fillMaxWidth()) { Text("Tomar foto") }
        OutlinedButton(enabled = !ocupado && fotos.size < maxFotos, onClick = {
            try { galeria.launch("image/*") } catch (_: Exception) { error = "No se pudo abrir la galería." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Elegir de la galería") }
        if (ocupado) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
    }
}
