package cl.louaraya.macrame

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Aviso emergente descartable. No ocupa espacio en el diseño del catálogo. */
@Composable
fun BannerOfertaCatalogo(onVerOferta: (String) -> Unit = {}) {
    val context = LocalContext.current
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var ofertas by remember { mutableStateOf(PromocionesLocal.activas(context)) }
    // Si el cliente la ignora o la abre, no vuelve a aparecer durante esta visita.
    var ultimaDescartada by rememberSaveable { mutableLongStateOf(-1L) }

    LaunchedEffect(Unit) {
        while (true) {
            ahora = System.currentTimeMillis()
            ofertas = PromocionesLocal.activas(context, ahora)
            delay(1000L)
        }
    }

    val oferta = ofertas.firstOrNull { it.id != ultimaDescartada } ?: return
    val restante = ((oferta.finMillis - ahora).coerceAtLeast(0L)) / 1000L
    if (restante <= 0L) return

    val animacion = rememberInfiniteTransition(label = "brilloPromocion")
    val brillo by animacion.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "brillo"
    )

    AlertDialog(
        onDismissRequest = { ultimaDescartada = oferta.id },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFFFAF7F2),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("✦ ¡OFERTA ESPECIAL! ✦", color = Color(0xFF9C6248),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(brillo))
                Spacer(Modifier.height(8.dp))
                Text(oferta.titulo, color = Color(0xFF123D35), fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(oferta.mensaje, color = Color(0xFF342D29), fontSize = 14.sp,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(18.dp))
                Text("¡Aprovecha! Termina en:", color = Color(0xFF123D35),
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    TiempoOferta(restante / 86400L, "DÍAS", Modifier.weight(1f))
                    TiempoOferta((restante % 86400L) / 3600L, "HORAS", Modifier.weight(1f))
                    TiempoOferta((restante % 3600L) / 60L, "MIN", Modifier.weight(1f))
                    TiempoOferta(restante % 60L, "SEG", Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    ultimaDescartada = oferta.id
                    onVerOferta(oferta.tipo)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF123D35))
            ) { Text("Ver oferta") }
        },
        dismissButton = {
            TextButton(onClick = { ultimaDescartada = oferta.id }) {
                Text("Ignorar", color = Color(0xFF9C6248))
            }
        }
    )
}

@Composable
private fun TiempoOferta(valor: Long, unidad: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Color(0xFF123D35), RoundedCornerShape(10.dp))
            .padding(vertical = 9.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor.toString().padStart(2, '0'), color = Color.White,
            fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(unidad, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp)
    }
}
