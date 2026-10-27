package cl.louaraya.macrame.ui.screens

import cl.louaraya.macrame.R
import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.viewmodel.*
import cl.louaraya.macrame.ui.components.*
import androidx.lifecycle.viewmodel.compose.viewModel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val VerdeOferta = Color(0xFF123D35)
private val FondoOferta = Color(0xFFFAF7F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEnviarPromocion(onVolver: () -> Unit) {
    val context = LocalContext.current
    val promociones: PromocionesViewModel = viewModel()
    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Matrimonios") }
    var finMillis by remember { mutableLongStateOf(0L) }
    var desplegado by remember { mutableStateOf(false) }
    var verHistorial by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf<OfertaLocal?>(null) }
    var historial by remember { mutableStateOf(promociones.listar()) }
    val formato = remember { SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale("es", "CL")) }
    val categorias = listOf("Matrimonios", "Festivales", "Hogar", "Viajes", "Todas las campañas")

    fun seleccionarFin() {
        val ahora = Calendar.getInstance()
        DatePickerDialog(context, { _, anio, mes, dia ->
            TimePickerDialog(context, { _, hora, minuto ->
                val fecha = Calendar.getInstance().apply {
                    set(anio, mes, dia, hora, minuto, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                finMillis = fecha.timeInMillis
            }, ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true).show()
        }, ahora.get(Calendar.YEAR), ahora.get(Calendar.MONTH), ahora.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis() - 60000L
        }.show()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(FondoOferta)
            .statusBarsPadding().navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) { Text("← Volver", color = VerdeOferta) }
            Spacer(Modifier.width(6.dp))
            Text("Enviar promoción", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = VerdeOferta)
        }
        HorizontalDivider()
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE6EEE8))) {
                Column(Modifier.padding(16.dp)) {
                    Text("✦ Crea una oferta especial", fontWeight = FontWeight.Bold, color = VerdeOferta)
                    Text("Los clientes verán la promoción en su catálogo hasta la fecha de término.", fontSize = 13.sp)
                }
            }
            OutlinedTextField(
                value = titulo, onValueChange = { if (it.length <= 70) titulo = it },
                label = { Text("Título") }, placeholder = { Text("¡20% de descuento!") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = mensaje, onValueChange = { if (it.length <= 300) mensaje = it },
                label = { Text("Mensaje") },
                placeholder = { Text("Aprovecha nuestra promoción de macramé") },
                modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 5,
                shape = RoundedCornerShape(14.dp)
            )
            ExposedDropdownMenuBox(expanded = desplegado, onExpandedChange = { desplegado = it }) {
                OutlinedTextField(
                    value = tipo, onValueChange = {}, readOnly = true,
                    label = { Text("Tipo de campaña") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegado) }
                )
                ExposedDropdownMenu(expanded = desplegado, onDismissRequest = { desplegado = false }) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(text = { Text(categoria) }, onClick = {
                            tipo = categoria
                            desplegado = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = { seleccionarFin() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (finMillis == 0L) "📅 Elegir fecha y hora de término"
                     else "Finaliza: ${formato.format(finMillis)}")
            }
            Text(
                "La oferta desaparecerá del catálogo cuando termine el tiempo.",
                fontSize = 12.sp, color = Color.Gray
            )
            Button(
                onClick = {
                    val oferta = OfertaLocal(
                        id = System.currentTimeMillis(), titulo = titulo.trim(),
                        mensaje = mensaje.trim(), tipo = tipo, finMillis = finMillis
                    )
                    if (promociones.guardar(oferta)) {
                        historial = promociones.listar()
                        titulo = ""
                        mensaje = ""
                        finMillis = 0L
                        Toast.makeText(context, "Oferta publicada en este dispositivo", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No se pudo guardar la oferta", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = titulo.isNotBlank() && mensaje.isNotBlank() && finMillis > System.currentTimeMillis(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeOferta)
            ) { Text("Publicar oferta") }
            OutlinedButton(onClick = {
                historial = promociones.listar()
                verHistorial = true
            }, modifier = Modifier.fillMaxWidth()) { Text("Ver ofertas y eliminar") }
            Text(
                "La oferta aparece en la bandeja de las cuentas de este dispositivo. Los avisos del teléfono requieren permiso y una sesión activa; no hay envío entre dispositivos.",
                fontSize = 11.sp, color = Color.Gray
            )
        }
    }

    if (verHistorial) {
        AlertDialog(
            onDismissRequest = { verHistorial = false },
            title = { Text("Ofertas publicadas") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (historial.isEmpty()) Text("No hay ofertas guardadas.")
                    historial.forEach { oferta ->
                        Card(colors = CardDefaults.cardColors(containerColor = FondoOferta)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(oferta.titulo, fontWeight = FontWeight.Bold, color = VerdeOferta)
                                Text(oferta.mensaje, fontSize = 12.sp)
                                Text("${oferta.tipo} · ${if (oferta.finMillis > 0) formato.format(oferta.finMillis) else "Sin fecha"}", fontSize = 11.sp)
                                Text(
                                    if (oferta.finMillis > System.currentTimeMillis()) "Activa" else "Finalizada",
                                    color = if (oferta.finMillis > System.currentTimeMillis()) Color(0xFF26834B) else Color.Gray
                                )
                                TextButton(onClick = { confirmarEliminar = oferta }) {
                                    Text("Eliminar oferta", color = Color(0xFFB3261E))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { verHistorial = false }) { Text("Cerrar") } }
        )
    }

    confirmarEliminar?.let { oferta ->
        AlertDialog(
            onDismissRequest = { confirmarEliminar = null },
            title = { Text("Eliminar oferta") },
            text = { Text("¿Eliminar «${oferta.titulo}»? Dejará de aparecer en el catálogo.") },
            confirmButton = {
                TextButton(onClick = {
                    if (promociones.eliminar(oferta.id)) {
                        historial = promociones.listar()
                        Toast.makeText(context, "Oferta eliminada", Toast.LENGTH_SHORT).show()
                    }
                    confirmarEliminar = null
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = null }) { Text("Cancelar") } }
        )
    }
}
