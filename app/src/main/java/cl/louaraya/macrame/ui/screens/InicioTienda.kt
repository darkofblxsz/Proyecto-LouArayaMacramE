package cl.louaraya.macrame.ui.screens

import android.content.SharedPreferences
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.louaraya.macrame.R
import cl.louaraya.macrame.model.OfertaLocal
import cl.louaraya.macrame.repository.NotificacionesLocal
import cl.louaraya.macrame.repository.PromocionesLocal
import cl.louaraya.macrame.ui.components.FotoProducto
import cl.louaraya.macrame.viewmodel.TiendaViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private data class Destino(val ruta: String, val texto: String, val icono: ImageVector)
private val destinos = listOf(Destino("inicio", "Inicio", Icons.Outlined.Home), Destino("catalogo", "Catálogo", Icons.Outlined.GridView),
    Destino("favoritos", "Favoritos", Icons.Outlined.FavoriteBorder), Destino("mapaFerias", "Mapa", Icons.Outlined.Place),
    Destino("misSolicitudes", "Pedidos", Icons.Outlined.ShoppingBag), Destino("perfil", "Perfil", Icons.Outlined.Person))

@Composable
fun MenuCliente(actual: String, navegar: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        destinos.forEach { d ->
            NavigationBarItem(selected = actual == d.ruta, onClick = { navegar(d.ruta) },
                icon = { Icon(d.icono, d.texto) }, label = { Text(d.texto, style = MaterialTheme.typography.labelSmall, maxLines = 1) })
        }
    }
}

@Composable
fun CabeceraTienda(vm: TiendaViewModel, navegar: (String) -> Unit) {
    val context = LocalContext.current
    val correo = vm.correoSesion
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(correo) {
        val prefs = context.getSharedPreferences("avisos_lou_araya", 0)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> if (key == "avisos") revision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val pendientes = remember(correo, revision) { correo?.let { NotificacionesLocal(context).todas(it).count { a -> !a.leida } } ?: 0 }
    Row(Modifier.fillMaxWidth().background(Color(0xFF123D35)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AvatarPerfil(vm.perfil()?.optString("foto"), Modifier.size(40.dp).clickable { navegar("perfil") })
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text("LOU ARAYA", color = Color.White, fontWeight = FontWeight.Bold)
            Text("MACRAMÉ", color = Color(0xFFEAD8BE), style = MaterialTheme.typography.labelSmall)
        }
        IconButton(onClick = { navegar("favoritos") }) { Icon(Icons.Outlined.FavoriteBorder, "Favoritos", tint = Color.White) }
        IconButton(onClick = { navegar("notificaciones") }) {
            BadgedBox(badge = { if (pendientes > 0) Badge { Text(if (pendientes > 99) "99+" else pendientes.toString()) } }) {
                Icon(Icons.Outlined.Notifications, "Notificaciones, $pendientes sin leer", tint = Color.White)
            }
        }
    }
}

@Composable
fun PantallaInicio(vm: TiendaViewModel, navegar: (String) -> Unit, pendientes: Int = 0) {
    val context = LocalContext.current
    var ahora by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); ahora = System.currentTimeMillis() } }
    val ofertas = PromocionesLocal.activas(context, ahora)
    var seleccionada by remember { mutableStateOf<OfertaLocal?>(null) }
    val escala = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) { escala.animateTo(1f, tween(900)) }
    val categorias = listOf(Triple("Hogar", Icons.Outlined.Home, "Hogar"), Triple("Matrimonios", Icons.Outlined.FavoriteBorder, "Matrimonios"),
        Triple("Festivales", Icons.Outlined.Celebration, "Festivales"), Triple("Viajes", Icons.Outlined.Flight, "Viajes"))
    LazyColumn(Modifier.fillMaxSize().background(Color(0xFFFAF7F2)), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("¡Hola, ${vm.perfil()?.optString("nombre") ?: "visitante"}!", style = MaterialTheme.typography.titleLarge)
                Text("Hecho a mano, elegido por ti.", style = MaterialTheme.typography.bodySmall)
            }
            Box(Modifier.fillMaxWidth().height(210.dp).clickable { navegar("catalogo") }) {
                Image(painterResource(R.drawable.fondo_bienvenida), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                Column(Modifier.padding(20.dp).align(Alignment.CenterStart)) {
                    Image(painterResource(R.drawable.la_mandala), "Logo Lou Araya", Modifier.size(64.dp).graphicsLayer { scaleX = escala.value; scaleY = escala.value })
                    Text("ARTE PARA TU HOGAR", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Descubre nuestras colecciones  ›", color = Color.White)
                }
            }
        }
        item {
            Text("Compra por categoría", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                categorias.forEach { (nombre, icono, filtro) ->
                    Column(Modifier.weight(1f).clickable { vm.categoriaInicio = filtro; navegar("catalogo") }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFECE5DD)), contentAlignment = Alignment.Center) { Icon(icono, nombre, Modifier.size(30.dp), tint = Color(0xFF123D35)) }
                        Text(nombre, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().background(Color(0xFFFFE3C0)).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalOffer, null, tint = Color(0xFF9C4829))
                    Text("  Promociones y descuentos", fontWeight = FontWeight.Bold)
                }
                Text(if (ofertas.isEmpty()) "Pronto encontrarás nuevas promociones aquí." else "Descubre las ofertas publicadas por Lou Araya.", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (ofertas.isNotEmpty()) item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ofertas, key = { it.id }) { oferta ->
                    Card(Modifier.width(270.dp).clickable { seleccionada = oferta }, colors = CardDefaults.cardColors(containerColor = Color(0xFF123D35))) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(oferta.titulo, color = Color(0xFFFFD59D), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(oferta.mensaje, color = Color.White, maxLines = 3)
                            val segundos = ((oferta.finMillis - ahora) / 1000).coerceAtLeast(0)
                            Text("Termina en %02d:%02d:%02d".format(segundos / 3600, segundos % 3600 / 60, segundos % 60), color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Text("Ver promoción  ›", color = Color.White)
                        }
                    }
                }
            }
        }
        item { Text("Descubre nuestros diseños", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium) }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.productos().filter { it.stock > 0 }.take(8), key = { it.id }) { p ->
                    Card(Modifier.width(155.dp).clickable { vm.categoriaInicio = p.categoria; navegar("catalogo") }, shape = RoundedCornerShape(14.dp)) {
                        FotoProducto(p.fotos.firstOrNull(), p.imagenKey, p.nombre, Modifier.fillMaxWidth().height(140.dp))
                        Column(Modifier.padding(10.dp)) {
                            Text(p.nombre, maxLines = 2, style = MaterialTheme.typography.titleSmall)
                            Text(NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(p.precio), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    seleccionada?.let { oferta ->
        AlertDialog(onDismissRequest = { seleccionada = null }, title = { Text(oferta.titulo) }, text = { Text(oferta.mensaje + "\n\nCategoría: " + oferta.tipo + if (oferta.finMillis <= ahora) "\nPromoción finalizada" else "") },
            confirmButton = { TextButton(enabled = oferta.finMillis > ahora, onClick = { vm.categoriaInicio = if (oferta.tipo == "Todas las campañas") "Todas" else oferta.tipo; seleccionada = null; navegar("catalogo") }) { Text("Ver productos") } },
            dismissButton = { TextButton(onClick = { seleccionada = null }) { Text("Cerrar") } })
    }
}
