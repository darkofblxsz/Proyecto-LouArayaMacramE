package cl.louaraya.macrame

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Locale

@Composable
fun FormularioSolicitud(producto: ProductoLocal, correo: String, onCerrar: () -> Unit, onCreada: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SolicitudesLocal(context) }
    var cantidad by rememberSaveable { mutableStateOf("1") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var comentario by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onCerrar, title = { Text("Solicitar ${producto.nombre}") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Disponible: ${producto.stock} unidades")
            Text("Cuenta: $correo", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(cantidad, { cantidad = it.filter(Char::isDigit) }, label = { Text("Cantidad") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            OutlinedButton(onClick = {
                val hoy = LocalDate.now()
                DatePickerDialog(context, { _, ano, mes, dia ->
                    fecha = String.format(Locale.ROOT, "%02d/%02d/%04d", dia, mes + 1, ano)
                }, hoy.year, hoy.monthValue - 1, hoy.dayOfMonth).apply {
                    datePicker.minDate = System.currentTimeMillis() - 1000
                }.show()
            }) { Text(if (fecha.isEmpty()) "Elegir fecha requerida" else fecha) }
            OutlinedTextField(comentario, { comentario = it.take(500) }, label = { Text("Comentario (opcional)") })
            Text("Se guardará como pendiente. El stock se reserva cuando el administrador confirma.", style = MaterialTheme.typography.bodySmall)
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = {
        Button(onClick = {
            val resultado = repo.crear(correo, producto.id, cantidad.toIntOrNull() ?: 0, fecha, comentario)
            if (resultado == null) onCreada() else error = resultado
        }) { Text("Crear solicitud") }
    }, dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } })
}

@Composable
fun PantallaSolicitudes(onVolver: () -> Unit, correoCliente: String? = null, administrador: Boolean = false) {
    val context = LocalContext.current
    val repo = remember { SolicitudesLocal(context) }
    var solicitudes by remember { mutableStateOf(repo.todas()) }
    var filtro by rememberSaveable { mutableStateOf("Todas") }
    var seleccionada by remember { mutableStateOf<SolicitudLocal?>(null) }
    var error by remember { mutableStateOf("") }
    val visibles = solicitudes.filter {
        (administrador || (correoCliente != null && it.correo == correoCliente)) &&
            (filtro == "Todas" || it.estado == filtro)
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFFAF7F2)).safeDrawingPadding().padding(16.dp)) {
        TextButton(onClick = onVolver) { Text("‹ Volver") }
        Text(if (administrador) "Gestión de solicitudes" else "Mis solicitudes", style = MaterialTheme.typography.headlineSmall)
        Text("Solicitudes guardadas en este dispositivo", style = MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(listOf("Todas", "Pendiente", "Confirmado", "Entregado", "Cancelado")) { estado ->
                FilterChip(selected = estado == filtro, onClick = { filtro = estado }, label = { Text(estado) })
            }
        }
        if (visibles.isEmpty()) Text("No hay solicitudes para mostrar.", Modifier.padding(vertical = 24.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visibles, key = { it.id }) { solicitud ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(solicitud.producto, style = MaterialTheme.typography.titleMedium)
                        Text("${solicitud.codigo} · ${solicitud.unidades} unidades")
                        Text("Fecha requerida: ${solicitud.fecha}")
                        if (administrador) Text("Cliente: ${solicitud.correo}")
                        Text("Estado: ${solicitud.estado}")
                        if (solicitud.comentario.isNotEmpty()) Text(solicitud.comentario)
                        if (administrador && solicitud.estado in listOf("Pendiente", "Confirmado")) {
                            OutlinedButton(onClick = { seleccionada = solicitud; error = "" }) { Text("Actualizar estado") }
                        }
                    }
                }
            }
        }
    }
    seleccionada?.let { s ->
        AlertDialog(onDismissRequest = { seleccionada = null }, title = { Text("Actualizar solicitud") }, text = {
            Column {
                Text(s.producto)
                Text("Confirmar reserva el stock. Cancelar una solicitud confirmada lo devuelve.")
                val estados = if (s.estado == "Pendiente") listOf("Confirmado", "Cancelado") else listOf("Entregado", "Cancelado")
                estados.forEach { nuevo ->
                    OutlinedButton(onClick = {
                        val resultado = repo.cambiarEstado(s.id, nuevo)
                        if (resultado == null) { solicitudes = repo.todas(); seleccionada = null }
                        else error = resultado
                    }) { Text(nuevo) }
                }
                if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            }
        }, confirmButton = { TextButton(onClick = { seleccionada = null }) { Text("Cerrar") } })
    }
}
