package cl.louaraya.macrame.ui.screens

import cl.louaraya.macrame.R
import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.viewmodel.*
import cl.louaraya.macrame.ui.components.*
import androidx.lifecycle.viewmodel.compose.viewModel

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Locale

@Composable
fun FormularioSolicitud(producto: ProductoLocal, correo: String, onCerrar: () -> Unit, onCreada: () -> Unit, existente: SolicitudLocal? = null) {
    val context = LocalContext.current
    val repo: SolicitudesViewModel = viewModel()
    var cantidad by rememberSaveable { mutableStateOf(existente?.unidades?.toString() ?: "1") }
    var fecha by rememberSaveable { mutableStateOf(existente?.fecha ?: "") }
    var comentario by rememberSaveable { mutableStateOf(existente?.comentario ?: "") }
    var personalizada by rememberSaveable { mutableStateOf(existente?.personalizada ?: false) }
    var color by rememberSaveable { mutableStateOf(existente?.color ?: "Original") }
    var material by rememberSaveable { mutableStateOf(existente?.material ?: "Original") }
    var filtroMaterial by rememberSaveable { mutableStateOf("") }
    var revisar by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    if (!revisar) AlertDialog(onDismissRequest = onCerrar, title = { Text(if (existente == null) "Solicitar ${producto.nombre}" else "Editar pedido") }, text = {
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
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = personalizada, onCheckedChange = { personalizada = it })
                Text("Quiero personalizar mi pedido")
            }
            if (!personalizada) Text("Se conservarán el color y el material originales del producto.")
            if (personalizada) {
                Text("Paleta de colores · seleccionado: $color")
                val paleta = listOf("Original" to Color(0xFFD6C6A8), "Crudo" to Color(0xFFF0E5CF), "Blanco" to Color.White,
                    "Beige" to Color(0xFFD2B48C), "Terracota" to Color(0xFFB85C38), "Verde" to Color(0xFF20634F), "Negro" to Color.Black)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(paleta) { (nombre, tono) ->
                        FilterChip(selected = color == nombre, onClick = { color = nombre }, label = { Text(nombre) },
                            leadingIcon = { Box(Modifier.size(18.dp).background(tono, androidx.compose.foundation.shape.CircleShape)) })
                    }
                }
                Text("Tipo de material · seleccionado: $material")
                OutlinedTextField(filtroMaterial, { filtroMaterial = it }, label = { Text("Filtrar materiales") }, singleLine = true)
                val materiales = (listOf("Original", producto.material, "Algodón natural", "Algodón reciclado", "Yute", "Algodón y madera")).distinct().filter { it.contains(filtroMaterial, true) }
                if (materiales.isEmpty()) Text("No hay materiales con ese nombre.")
                materiales.forEach { opcion ->
                    FilterChip(selected = material == opcion, onClick = { material = opcion }, label = { Text(opcion) })
                }
                Text("La personalización está sujeta a confirmación del administrador; el precio del catálogo es referencial.", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(comentario, { comentario = it.take(500) }, label = { Text("Comentario (opcional)") })
            Text("Se guardará como pendiente. El stock se reserva cuando el administrador confirma.", style = MaterialTheme.typography.bodySmall)
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = {
        Button(onClick = {
            error = when {
                (cantidad.toIntOrNull() ?: 0) !in 1..producto.stock -> "Ingresa una cantidad entre 1 y ${producto.stock}."
                !cl.louaraya.macrame.repository.fechaSolicitudValida(fecha) -> "Selecciona una fecha válida desde hoy."
                else -> ""
            }
            if (error.isEmpty()) revisar = true
        }) { Text("Revisar pedido") }
    }, dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } })
    if (revisar) AlertDialog(onDismissRequest = { revisar = false }, title = { Text("Confirma tu pedido") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${producto.nombre} · $cantidad unidades")
            Text("Fecha: $fecha")
            Text(if (personalizada) "Personalizado: $color · $material" else "Diseño original: ${producto.material}")
            if (comentario.isNotBlank()) Text(comentario)
            Text("Solo se guardará cuando pulses Confirmar.")
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { Button(onClick = {
        val resultado = if (existente == null) repo.crear(correo, producto.id, cantidad.toIntOrNull() ?: 0, fecha, comentario, personalizada, color, material)
            else repo.modificar(existente.id, correo, cantidad.toIntOrNull() ?: 0, fecha, comentario, personalizada, color, material)
        if (resultado == null) onCreada() else error = resultado
    }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { revisar = false }) { Text("Volver a editar") } })
}

@Composable
fun PantallaSolicitudes(onVolver: () -> Unit, correoCliente: String? = null, administrador: Boolean = false) {
    val context = LocalContext.current
    val repo: SolicitudesViewModel = viewModel()
    var solicitudes by remember { mutableStateOf(repo.todas()) }
    var filtro by rememberSaveable { mutableStateOf("Todas") }
    var editar by remember { mutableStateOf<SolicitudLocal?>(null) }
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
                        Text(if (solicitud.personalizada) "Personalización: ${solicitud.color} · ${solicitud.material}" else "Diseño original")
                        if (!administrador && solicitud.estado == "Pendiente") {
                            OutlinedButton(onClick = { editar = solicitud }) { Text("Modificar mi pedido") }
                        }
                        if (solicitud.comentario.isNotEmpty()) Text(solicitud.comentario)
                        if (administrador && solicitud.estado in listOf("Pendiente", "Confirmado")) {
                            OutlinedButton(onClick = { seleccionada = solicitud; error = "" }) { Text("Actualizar estado") }
                        }
                    }
                }
            }
        }
    }
    editar?.let { s ->
        val producto = cl.louaraya.macrame.repository.AlmacenLocal(context).productos().firstOrNull { it.id == s.productoId }
        if (producto != null) FormularioSolicitud(producto, correoCliente ?: "", onCerrar = { editar = null },
            onCreada = { editar = null; solicitudes = repo.todas() }, existente = s)
        else AlertDialog(onDismissRequest = { editar = null }, title = { Text("Producto no disponible") },
            text = { Text("El producto fue retirado del catálogo. No se puede modificar esta solicitud.") },
            confirmButton = { TextButton(onClick = { editar = null }) { Text("Cerrar") } })
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
