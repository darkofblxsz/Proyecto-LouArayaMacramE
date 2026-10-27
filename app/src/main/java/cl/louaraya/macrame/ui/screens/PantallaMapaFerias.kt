package cl.louaraya.macrame.ui.screens

import android.content.Context
import android.location.Geocoder
import java.util.Locale
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Storefront
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import android.graphics.drawable.GradientDrawable
import android.graphics.Color as AndroidColor
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private data class Feria(val id: Int, val nombre: String, val tipo: String, val fecha: String, val lugar: String, val lat: Double, val lon: Double, val descripcion: String = "", val fechaFin: String = "")

/** Caché SQLite persistente; conserva la última respuesta REST válida. */
private class FeriasDb(context: Context) : SQLiteOpenHelper(context, "ferias_lou_araya.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ferias (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL, tipo TEXT NOT NULL, fecha TEXT NOT NULL, lugar TEXT NOT NULL, lat REAL NOT NULL, lon REAL NOT NULL, descripcion TEXT NOT NULL DEFAULT '', fechaFin TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE TABLE inscripciones (evento_id INTEGER NOT NULL, correo TEXT NOT NULL, PRIMARY KEY(evento_id,correo))")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) db.execSQL("CREATE TABLE IF NOT EXISTS inscripciones (evento_id INTEGER NOT NULL, correo TEXT NOT NULL, PRIMARY KEY(evento_id,correo))")
    }
    fun estaInscrito(id: Int, correo: String): Boolean = readableDatabase.rawQuery(
        "SELECT 1 FROM inscripciones WHERE evento_id=? AND correo=?", arrayOf(id.toString(), correo)
    ).use { it.moveToFirst() }
    fun inscribir(id: Int, correo: String) {
        val values = android.content.ContentValues().apply { put("evento_id", id); put("correo", correo) }
        writableDatabase.insertWithOnConflict("inscripciones", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }
    fun cancelarInscripcion(id: Int, correo: String) { writableDatabase.delete("inscripciones", "evento_id=? AND correo=?", arrayOf(id.toString(), correo)) }
    fun contarInscritos(id: Int): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM inscripciones WHERE evento_id=?", arrayOf(id.toString())).use { it.moveToFirst(); it.getInt(0) }
    fun guardar(ferias: List<Feria>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("ferias", null, null)
            ferias.forEach { f ->
                val v = android.content.ContentValues().apply {
                    put("id", f.id); put("nombre", f.nombre); put("tipo", f.tipo); put("fecha", f.fecha)
                    put("lugar", f.lugar); put("lat", f.lat); put("lon", f.lon); put("descripcion", f.descripcion); put("fechaFin", f.fechaFin)
                }
                db.insertOrThrow("ferias", null, v)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    fun guardarUno(f: Feria) {
        val values = android.content.ContentValues().apply {
            put("id", f.id); put("nombre", f.nombre); put("tipo", f.tipo)
            put("fecha", f.fecha); put("fechaFin", f.fechaFin)
            put("lugar", f.lugar); put("lat", f.lat); put("lon", f.lon)
            put("descripcion", f.descripcion)
        }
        writableDatabase.insertWithOnConflict("ferias", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun eliminarUno(id: Int) { writableDatabase.delete("ferias", "id=?", arrayOf(id.toString())) }
    fun leer(): List<Feria> {
        val items = mutableListOf<Feria>()
        readableDatabase.rawQuery("SELECT id,nombre,tipo,fecha,lugar,lat,lon,descripcion,fechaFin FROM ferias", null).use { c ->
            while(c.moveToNext()) items.add(Feria(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getDouble(5),c.getDouble(6),c.getString(7),c.getString(8)))
        }
        return items
    }
}

/** Cambiar esta URL por la dirección HTTPS real del backend que devuelva un array JSON de ferias. */
private const val FERIAS_API_URL = ""
private val eventoDemo = Feria(-1, "Feria artesanal (DEMOSTRACIÓN)", "Feria", "Fecha de prueba · sin confirmar", "Parque Bicentenario, Vitacura (ubicación ilustrativa)", -33.4014, -70.6008)

private fun cargarApi(): List<Feria> {
    if (FERIAS_API_URL.isBlank()) return emptyList()
    val conn = URL(FERIAS_API_URL).openConnection() as HttpURLConnection
    try {
        conn.connectTimeout = 7000; conn.readTimeout = 7000
        if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
        val array = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
        return (0 until array.length()).map { i ->
            val j = array.getJSONObject(i)
            Feria(j.getInt("id"),j.getString("nombre"),j.optString("tipo","Feria"),j.getString("fecha"),j.getString("lugar"),j.getDouble("lat"),j.getDouble("lon"),j.optString("descripcion"),j.optString("fechaFin"))
        }
    } finally { conn.disconnect() }
}


private val terracota = Color(0xFF9E3F2C)
private val crema = Color(0xFFFFF7EE)
private val verde = Color(0xFF397B5B)

/** Pantalla integrada: lista de entrada y mapa a pantalla completa, con evento de demostración separado de REST y caché. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapaFerias(onVolver: () -> Unit, esAdministrador: Boolean = false, correoCliente: String? = null, onIniciarSesion: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var editarEvento by remember { mutableStateOf<Feria?>(null) }
    var confirmarEliminar by remember { mutableStateOf<Feria?>(null) }
    var aviso by remember { mutableStateOf("") }
    var versionInscripciones by remember { mutableIntStateOf(0) }

    val db = remember { FeriasDb(context.applicationContext) }
    var eventos by remember { mutableStateOf<List<Feria>>(emptyList()) }
    var filtro by remember { mutableStateOf("Todos") }
    var seleccionado by remember { mutableStateOf<Feria?>(null) }
    var verMapa by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf("Cargando ferias…") }
    var cargando by remember { mutableStateOf(true) }
    // Solo el AndroidView visible es propietario del mapa. Nunca reutilizar un MapView destruido.
    var activeMap by remember { mutableStateOf<MapView?>(null) }
    LaunchedEffect(Unit) {
        val cache = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { db.leer() }
        eventos = cache
        if (FERIAS_API_URL.isBlank()) {
            estado = if (cache.isEmpty()) "Evento demostrativo · configura la API para ferias reales" else "Eventos guardados en SQLite · API sin configurar"
        } else {
            try {
                val respuesta = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { cargarApi() }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { db.guardar(respuesta) }
                eventos = respuesta
                estado = "Eventos actualizados desde API REST"
            } catch (_: Exception) {
                estado = if (cache.isEmpty()) "Sin conexión · no hay eventos guardados" else "Sin conexión · mostrando caché SQLite"
            }
        }
        cargando = false
    }
    fun abrirNuevo() { editarEvento = null; mostrarFormulario = true }
    fun abrirEdicion(f: Feria) { editarEvento = f; mostrarFormulario = true }
    fun guardarEvento(f: Feria) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (FERIAS_API_URL.isNotBlank()) enviarCambioApi(if (editarEvento == null) "POST" else "PUT", f)
                    db.guardarUno(f)
                }
                eventos = withContext(Dispatchers.IO) { db.leer() }
                aviso = if (FERIAS_API_URL.isBlank()) "Guardado localmente (API pendiente)" else "Evento sincronizado con API"
                mostrarFormulario = false
            } catch (e: Exception) { aviso = "No se pudo guardar: ${e.message ?: "error de conexión"}" }
        }
    }
    fun borrarEvento(f: Feria) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (FERIAS_API_URL.isNotBlank()) enviarCambioApi("DELETE", f)
                    db.eliminarUno(f.id)
                }
                eventos = withContext(Dispatchers.IO) { db.leer() }
                seleccionado = null
                aviso = "Evento eliminado"
            } catch (e: Exception) { aviso = "No se pudo eliminar: ${e.message ?: "error de conexión"}" }
            confirmarEliminar = null
        }
    }
    val demostracion = !cargando && eventos.isEmpty() && FERIAS_API_URL.isBlank()
    val disponibles = if (demostracion) listOf(eventoDemo) else eventos
    val visibles = disponibles.filter { filtro == "Todos" || it.tipo.equals(filtro, ignoreCase = true) ||
        (filtro == "Evento" && it.tipo.lowercase() in listOf("exposición", "exposicion")) }

    DisposableEffect(db) {
        onDispose { db.close() }
    }
    val marron = Color(0xFF4B3028)
    val fondo = Color(0xFFFFF8F1)

    Column(Modifier.fillMaxSize().background(fondo).statusBarsPadding().navigationBarsPadding()) {
        if (!verMapa) {
            Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onVolver, contentPadding = PaddingValues(4.dp)) {
                    Text("‹  Volver", color = terracota, fontSize = 14.sp)
                }
                Text(if (esAdministrador) "Gestión de ferias" else "Ferias y Showrooms", modifier = Modifier.weight(1f),
                    fontSize = 17.sp, fontWeight = FontWeight.Bold, color = marron,
                    maxLines = 1)
                TextButton(onClick = { verMapa = true }) { Text("Mapa", color = terracota) }
            }
            Text(if (esAdministrador) "Administra los eventos de Lou Araya Macramé" else "Descubre dónde encontrar nuestro macramé", Modifier.padding(start = 20.dp, bottom = 8.dp),
                color = Color.DarkGray, fontSize = 12.sp)
            FiltrosLou(filtro, { filtro = it; seleccionado = null })
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (esAdministrador) "Eventos registrados" else "Próximas ferias y showrooms",
                        modifier = Modifier.weight(1f), color = marron, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (esAdministrador) FilledTonalButton(onClick = { abrirNuevo() }) {
                        Icon(Icons.Default.Add, null); Text("Nuevo")
                    }
                }
                if (aviso.isNotBlank()) Text(aviso, color = terracota, fontSize = 12.sp)
                if (cargando) Text("Cargando eventos…")
                if (!cargando && visibles.isEmpty()) Text("No hay eventos disponibles para este filtro.", color = Color.Gray)
                visibles.forEach { feria ->
                    Card(shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(feria.nombre, color = marron, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("📅  ${feria.fecha}", fontSize = 13.sp)
                            Text("📍  ${feria.lugar}", fontSize = 13.sp)
                            Text("${feria.tipo}  ·  Lou Araya Macramé", color = verde, fontSize = 12.sp)
                            if (feria.id == -1) Text("Solo para pruebas; no es una feria confirmada.",
                                fontSize = 11.sp, color = terracota)
                            if (esAdministrador && feria.id != -1) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { abrirEdicion(feria) }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Edit, null); Text("Editar")
                                    }
                                    OutlinedButton(onClick = { confirmarEliminar = feria }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Delete, null); Text("Eliminar")
                                    }
                                }
                            }
                            if (feria.tipo.equals("Evento", ignoreCase = true) && !esAdministrador && feria.id != -1) {
                                val inscrito = remember(feria.id, correoCliente, versionInscripciones) {
                                    correoCliente?.let { db.estaInscrito(feria.id, it) } ?: false
                                }
                                Button(onClick = {
                                    if (correoCliente.isNullOrBlank()) onIniciarSesion()
                                    else scope.launch {
                                        withContext(Dispatchers.IO) {
                                            if (inscrito) db.cancelarInscripcion(feria.id, correoCliente)
                                            else db.inscribir(feria.id, correoCliente)
                                        }
                                        versionInscripciones++
                                        aviso = if (inscrito) "Inscripción cancelada" else "Inscripción guardada en este dispositivo"
                                    }
                                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = verde)) {
                                    Text(if (inscrito) "Cancelar inscripción" else if (correoCliente.isNullOrBlank()) "Inicia sesión para inscribirte" else "Inscribirme al evento")
                                }
                            }
                            if (esAdministrador && feria.tipo.equals("Evento", ignoreCase = true) && feria.id != -1) {
                                Text("Inscritos en este dispositivo: ${db.contarInscritos(feria.id)}", fontSize = 12.sp, color = marron)
                            }
                            Button(onClick = { seleccionado = feria; verMapa = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = terracota),
                                shape = RoundedCornerShape(14.dp)) { Text("Ver en el mapa") }
                        }
                    }
                }
            }
        } else {
            // El mapa ocupa todo el cuerpo, sin contenedores flotantes ni márgenes exteriores.
            Box(Modifier.fillMaxWidth().weight(1f)) {
                AndroidView(
                    factory = { androidContext ->
                        Configuration.getInstance().userAgentValue = androidContext.packageName
                        MapView(androidContext).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            minZoomLevel = 3.0
                            maxZoomLevel = 19.0
                            controller.setZoom(13.5)
                            controller.setCenter(GeoPoint(-33.4489, -70.6693))
                            onResume()
                            activeMap = this
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        // update se ejecuta con un MapView real, sin post() ni referencias nulas.
                        view.overlays.clear()
                        visibles.forEach { f ->
                            val color = when (f.tipo.lowercase()) {
                                "showroom" -> AndroidColor.rgb(57, 123, 91)
                                "evento", "exposición", "exposicion" -> AndroidColor.rgb(216, 123, 56)
                                else -> AndroidColor.rgb(158, 63, 44)
                            }
                            val pin = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setSize(42, 42)
                                setColor(color)
                                setStroke(4, AndroidColor.WHITE)
                            }
                            view.overlays.add(Marker(view).apply {
                                position = GeoPoint(f.lat, f.lon)
                                title = f.nombre
                                snippet = f.lugar
                                icon = pin
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                setOnMarkerClickListener { _, map ->
                                    seleccionado = f
                                    map.controller.animateTo(GeoPoint(f.lat, f.lon))
                                    true
                                }
                            })
                        }
                        val objetivo = seleccionado ?: visibles.firstOrNull()
                        if (objetivo != null) {
                            view.controller.animateTo(GeoPoint(objetivo.lat, objetivo.lon))
                        }
                        view.invalidate()
                    },
                    onRelease = { view ->
                        if (activeMap === view) activeMap = null
                        view.onPause()
                        view.overlays.clear()
                        view.onDetach()
                    }
                )
                Surface(Modifier.align(Alignment.TopStart).padding(14.dp).size(46.dp)
                    .clickable { seleccionado = null; verMapa = false },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White, shadowElevation = 6.dp) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("‹", fontSize = 30.sp, color = terracota, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(Modifier.align(Alignment.TopCenter).padding(top = 18.dp, start = 66.dp, end = 12.dp),
                    color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 4.dp) {
                    Text("Mapa de ferias", Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        color = marron, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Surface(Modifier.align(Alignment.TopEnd).padding(top = 78.dp, end = 12.dp),
                    shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 3.dp) {
                    Column(Modifier.padding(horizontal = 4.dp)) {
                        TextButton(onClick = { activeMap?.controller?.zoomIn() }) { Text("+", color = terracota, fontSize = 23.sp) }
                        TextButton(onClick = { activeMap?.controller?.zoomOut() }) { Text("−", color = terracota, fontSize = 23.sp) }
                    }
                }
                if (esAdministrador) FloatingActionButton(onClick = { abrirNuevo() },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = if (seleccionado == null) 20.dp else 210.dp),
                    containerColor = terracota, contentColor = Color.White) { Icon(Icons.Default.Add, "Nuevo evento") }
                seleccionado?.let { feria ->
                    Card(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(14.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(feria.nombre, Modifier.weight(1f), color = marron,
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                TextButton(onClick = { seleccionado = null }) { Text("✕", color = terracota) }
                            }
                            Text("📅 ${feria.fecha}", fontSize = 13.sp)
                            Text("📍 ${feria.lugar}", fontSize = 13.sp)
                            if (feria.id == -1) Text("Evento de demostración", color = terracota, fontSize = 12.sp)
                            Button(onClick = {
                                val uri = Uri.parse("geo:${feria.lat},${feria.lon}?q=${feria.lat},${feria.lon}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
                            }, modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = terracota)) { Text("Cómo llegar") }
                        }
                    }
                }
                if (!cargando && visibles.isEmpty()) {
                    Surface(Modifier.align(Alignment.Center).padding(24.dp),
                        color = Color.White, shape = RoundedCornerShape(14.dp)) {
                        Text("No hay eventos para mostrar", Modifier.padding(16.dp), color = marron)
                    }
                }
            }
        }
        // Barra persistente con iconos, del mismo lenguaje visual del catálogo.
        Row(Modifier.fillMaxWidth().height(66.dp).background(Color.White),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            NavLou(if (esAdministrador) "▦" else "⌂", if (esAdministrador) "Panel admin" else "Catálogo", false) { onVolver() }
            NavLou("☷", "Ferias", !verMapa) { verMapa = false; seleccionado = null }
            NavLou("◉", "Mapa", verMapa) { verMapa = true }
        }
        Text(estado, Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 3.dp),
            color = Color.Gray, fontSize = 9.sp, maxLines = 1)
    }
    if (esAdministrador && mostrarFormulario) {
        ModalBottomSheet(onDismissRequest = { mostrarFormulario = false },
            containerColor = crema, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            FormularioEventoLou(context, editarEvento, onCancelar = { mostrarFormulario = false }, onGuardar = ::guardarEvento)
        }
    }
    confirmarEliminar?.let { f ->
        AlertDialog(onDismissRequest = { confirmarEliminar = null },
            title = { Text("¿Eliminar evento?") },
            text = { Text("Se eliminará ${f.nombre}. Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { borrarEvento(f) }) { Text("Eliminar", color = terracota) } },
            dismissButton = { TextButton(onClick = { confirmarEliminar = null }) { Text("Cancelar") } })
    }
}

/** Los cambios se envían al backend cuando se configura FERIAS_API_URL.
 * El servidor debe validar autenticación y permisos de administrador. */
private fun enviarCambioApi(metodo: String, f: Feria) {
    val endpoint = if (metodo == "POST") FERIAS_API_URL else "${FERIAS_API_URL.trimEnd('/')}/${f.id}"
    val conn = URL(endpoint).openConnection() as HttpURLConnection
    try {
        conn.requestMethod = metodo
        conn.connectTimeout = 7000
        conn.readTimeout = 7000
        if (metodo != "DELETE") {
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val json = JSONObject().apply {
                put("id", f.id); put("nombre", f.nombre); put("tipo", f.tipo)
                put("fecha", f.fecha); put("fechaFin", f.fechaFin)
                put("lugar", f.lugar); put("lat", f.lat); put("lon", f.lon)
                put("descripcion", f.descripcion)
            }
            conn.outputStream.use { it.write(json.toString().toByteArray(Charsets.UTF_8)) }
        }
        if (conn.responseCode !in 200..299) error("API HTTP ${conn.responseCode}")
    } finally { conn.disconnect() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioEventoLou(context: Context, actual: Feria?, onCancelar: () -> Unit, onGuardar: (Feria) -> Unit) {
    var nombre by remember(actual) { mutableStateOf(actual?.nombre ?: "") }
    var tipo by remember(actual) { mutableStateOf(if (actual?.tipo in listOf("Evento", "Feria", "Showroom", "Exposición")) actual!!.tipo else if (actual == null) "Feria" else "Otro") }
    var otroTipo by remember(actual) { mutableStateOf(if (tipo == "Otro") actual?.tipo ?: "" else "") }
    var fecha by remember(actual) { mutableStateOf(actual?.fecha ?: "") }
    var fechaFin by remember(actual) { mutableStateOf(actual?.fechaFin ?: "") }
    var mostrarCalendario by remember { mutableStateOf(false) }
    var lugar by remember(actual) { mutableStateOf(actual?.lugar ?: "") }
    var latitud by remember(actual) { mutableStateOf(actual?.lat?.toString() ?: "") }
    var longitud by remember(actual) { mutableStateOf(actual?.lon?.toString() ?: "") }
    var descripcion by remember(actual) { mutableStateOf(actual?.descripcion ?: "") }
    var error by remember { mutableStateOf("") }
    if (mostrarCalendario) {
        CalendarioRangoLou(
            inicioActual = fecha,
            finActual = fechaFin,
            onCerrar = { mostrarCalendario = false },
            onAceptar = { inicio, fin ->
                fecha = inicio
                fechaFin = fin
                mostrarCalendario = false
            }
        )
    }
    Column(Modifier.fillMaxWidth().fillMaxHeight(0.86f).verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (actual == null) "Nuevo evento" else "Editar evento", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Lou Araya Macramé · Administración", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onCancelar) { Icon(Icons.Default.Close, "Cerrar") }
        }
        HorizontalDivider()
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre del evento") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Tipo de evento", fontWeight = FontWeight.SemiBold)
        listOf("Evento", "Feria", "Showroom", "Exposición", "Otro").forEach { opcion ->
            Row(Modifier.fillMaxWidth().height(38.dp).clickable { tipo = opcion }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = tipo == opcion, onClick = { tipo = opcion })
                Text(opcion)
            }
        }
        if (tipo == "Otro") OutlinedTextField(otroTipo, { otroTipo = it }, label = { Text("Escribe el tipo de evento") }, modifier = Modifier.fillMaxWidth())
        if (tipo == "Evento") {
            Text("Fechas del evento", fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = { mostrarCalendario = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (fecha.isNotBlank() && fechaFin.isNotBlank())
                    "${fecha}  →  ${fechaFin}" else "📅 Elegir fechas en el calendario")
            }
            Text("Selecciona el primer y último día en un solo calendario.", fontSize = 11.sp, color = Color.Gray)
        }
        OutlinedTextField(lugar, { lugar = it }, label = { Text("Dirección (calle, número, comuna, ciudad)") }, modifier = Modifier.fillMaxWidth())
        Text("La dirección se buscará automáticamente para ubicar el marcador.", fontSize = 11.sp, color = Color.Gray)
        OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        if (error.isNotBlank()) Text(error, color = terracota, fontSize = 12.sp)
        Button(onClick = {
            val tipoFinal = if (tipo == "Otro") otroTipo.trim() else tipo
            if (nombre.isBlank() || tipoFinal.isBlank() || lugar.isBlank() ||
                (tipo == "Evento" && (fecha.isBlank() || fechaFin.isBlank() || fechaFin < fecha))) {
                error = "Completa nombre, tipo y dirección. Los eventos requieren fechas válidas (AAAA-MM-DD)."
            } else {
                try {
                    @Suppress("DEPRECATION")
                    val resultados = Geocoder(context, Locale("es", "CL")).getFromLocationName(lugar.trim(), 1)
                    val direccion = resultados?.firstOrNull()
                    if (direccion == null) error = "No se encontró esa dirección. Escribe calle, comuna y ciudad e inténtalo de nuevo."
                    else onGuardar(Feria(actual?.id ?: (System.currentTimeMillis() % 2000000000L).toInt(),
                        nombre.trim(), tipoFinal, if (tipo == "Evento") fecha.trim() else "Ubicación permanente",
                        lugar.trim(), direccion.latitude, direccion.longitude, descripcion.trim(), if (tipo == "Evento") fechaFin.trim() else ""))
                } catch (e: Exception) { error = "No se pudo localizar la dirección. Comprueba la conexión e inténtalo otra vez." }
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = terracota)) { Text("Guardar evento") }
        Spacer(Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarioRangoLou(
    inicioActual: String,
    finActual: String,
    onCerrar: () -> Unit,
    onAceptar: (String, String) -> Unit
) {
    fun millis(fecha: String): Long? = try {
        LocalDate.parse(fecha).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    } catch (_: Exception) { null }
    val estado = rememberDateRangePickerState(
        initialSelectedStartDateMillis = millis(inicioActual),
        initialSelectedEndDateMillis = millis(finActual)
    )
    val formato = DateTimeFormatter.ISO_LOCAL_DATE
    fun fechaIso(ms: Long): String = Instant.ofEpochMilli(ms)
        .atZone(ZoneOffset.UTC).toLocalDate().format(formato)
    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                enabled = estado.selectedStartDateMillis != null && estado.selectedEndDateMillis != null,
                onClick = {
                    val desde = estado.selectedStartDateMillis ?: return@TextButton
                    val hasta = estado.selectedEndDateMillis ?: return@TextButton
                    onAceptar(fechaIso(desde), fechaIso(hasta))
                }
            ) { Text("Confirmar", color = terracota) }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    ) {
        DateRangePicker(
            state = estado,
            modifier = Modifier.width(360.dp).height(490.dp),
            title = { Text("Elige las fechas del evento", modifier = Modifier.padding(start = 20.dp, top = 12.dp)) },
            headline = { Text("Inicio  →  Término", modifier = Modifier.padding(horizontal = 20.dp)) },
            showModeToggle = false
        )
    }
}

@Composable
private fun FiltrosLou(seleccion: String, cambiar: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf("Todos", "Feria", "Showroom", "Evento").forEach { tipo ->
            FilterChip(selected = seleccion == tipo, onClick = { cambiar(tipo) },
                label = { Text(when(tipo) { "Feria" -> "Ferias"; "Showroom" -> "Showrooms"; "Evento" -> "Eventos"; else -> "Todos" },
                    fontSize = 10.sp, maxLines = 1) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = terracota,
                    selectedLabelColor = Color.White))
        }
    }
}

@Composable
private fun NavLou(simbolo: String, nombre: String, activo: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(simbolo, color = if (activo) terracota else Color.Gray, fontSize = 22.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal)
        Text(nombre, color = if (activo) terracota else Color.Gray, fontSize = 11.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal)
    }
}

