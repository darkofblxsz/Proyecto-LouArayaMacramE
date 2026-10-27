package cl.louaraya.macrame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import cl.louaraya.macrame.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage
import java.io.File
import cl.louaraya.macrame.viewmodel.TiendaViewModel
import cl.louaraya.macrame.ui.components.EditorFotos

@Composable
fun AvatarPerfil(foto: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(modifier.size(48.dp).clip(CircleShape).background(Color(0xFFDCE7DC)), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Person, "Perfil", Modifier.size(30.dp))
        if (!foto.isNullOrBlank()) AsyncImage(model = File(context.filesDir, "productos/$foto"),
            contentDescription = "Foto de perfil", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun PantallaPerfil(vm: TiendaViewModel, volver: () -> Unit) {
    val perfil = vm.perfil()
    var nombre by rememberSaveable { mutableStateOf(perfil?.optString("nombre") ?: "") }
    var apellido by rememberSaveable { mutableStateOf(perfil?.optString("apellido") ?: "") }
    var telefono by rememberSaveable { mutableStateOf(perfil?.optString("telefono") ?: "") }
    var foto by rememberSaveable { mutableStateOf(perfil?.optString("foto") ?: "") }
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var fallo by remember { mutableStateOf(false) }
    var ocupado by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = volver) { Text("‹ Volver") }
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
        AvatarPerfil(foto, Modifier.size(90.dp))
        Text(perfil?.optString("correo") ?: "")
        Text("Tu correo identifica tus pedidos y se mantiene asociado a tu cuenta.", style = MaterialTheme.typography.bodySmall)
        EditorFotos(if (foto.isBlank()) emptyList() else listOf(foto), "hogar", { foto = it.lastOrNull() ?: "" }, { ocupado = it }, maxFotos = 1, titulo = "Foto de perfil")
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(apellido, { apellido = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
        Text("Cambiar contraseña (opcional)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(actual, { actual = it }, label = { Text("Contraseña actual") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nueva, { nueva = it }, label = { Text("Nueva contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(confirmar, { confirmar = it }, label = { Text("Repite la nueva contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        if (mensaje.isNotEmpty()) Text(mensaje, color = if (fallo) MaterialTheme.colorScheme.error else Color(0xFF123D35))
        Button(enabled = !ocupado, onClick = {
            val error = if (nueva != confirmar) "Las nuevas contraseñas no coinciden." else vm.actualizarPerfil(nombre, apellido, telefono, foto, actual, nueva)
            fallo = error != null; mensaje = error ?: "Perfil guardado."
            if (error == null) { actual = ""; nueva = ""; confirmar = "" }
        }) { Text("Guardar cambios") }
        OutlinedButton(onClick = { vm.cerrarSesion() }) { Text("Cerrar sesión") }
    }
}
