package cl.louaraya.macrame.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import cl.louaraya.macrame.repository.AvisoLocal
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.louaraya.macrame.MainActivity
import cl.louaraya.macrame.R
import cl.louaraya.macrame.repository.NotificacionesLocal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ObservarAvisos(correo: String?) {
    val context = LocalContext.current
    DisposableEffect(correo) {
        val prefs = context.getSharedPreferences("avisos_lou_araya", 0)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll() // No mostrar avisos de la sesión anterior.
        fun mostrar() {
            if (correo == null) return
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
            if (!manager.areNotificationsEnabled()) return
            val repo = NotificacionesLocal(context)
            val avisos = repo.todas(correo).filter { !it.leida && !prefs.getBoolean("mostrada_" + it.id, false) }
            if (avisos.isEmpty()) return
            manager.createNotificationChannel(NotificationChannel("pedidos", "Pedidos y promociones", NotificationManager.IMPORTANCE_DEFAULT))
            val intent = Intent(context, MainActivity::class.java).putExtra("aviso_id", avisos.first().id).putExtra("aviso_correo", correo).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val abrir = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            try {
                manager.notify(100, NotificationCompat.Builder(context, "pedidos")
                    .setSmallIcon(R.drawable.ic_notificacion).setContentTitle("Lou Araya")
                    .setContentText("Tienes novedades. Toca para ver la notificación.")
                    .setContentIntent(abrir).setAutoCancel(true).build())
                val editor = prefs.edit()
                avisos.forEach { editor.putBoolean("mostrada_" + it.id, true) }
                editor.apply()
            } catch (_: SecurityException) { /* La bandeja sigue disponible sin permiso. */ }
        }
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> if (key == "avisos" || key == "permiso_actualizado") mostrar() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        mostrar()
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}

@Composable
fun PantallaNotificaciones(correo: String, volver: () -> Unit, avisoInicial: String? = null) {
    val context = LocalContext.current
    val repo = remember { NotificacionesLocal(context) }
    var avisos by remember(correo) { mutableStateOf(repo.todas(correo)) }
    var abierta by remember { mutableStateOf<AvisoLocal?>(null) }
    LaunchedEffect(avisoInicial, correo) {
        if (avisoInicial != null) {
            abierta = repo.todas(correo).firstOrNull { it.id == avisoInicial }
            if (abierta != null) repo.marcarLeida(correo, avisoInicial)
        }
    }
    var permisoMensaje by remember { mutableStateOf("") }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) context.getSharedPreferences("avisos_lou_araya", 0).edit().putLong("permiso_actualizado", System.currentTimeMillis()).apply()
        permisoMensaje = if (ok) "Notificaciones habilitadas. Los nuevos avisos aparecerán en tu teléfono." else "Puedes seguir leyendo los avisos aquí. Para habilitarlos, usa los ajustes de la app."
    }
    DisposableEffect(correo) {
        val prefs = context.getSharedPreferences("avisos_lou_araya", 0)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> if (key == "avisos") avisos = repo.todas(correo) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = volver) { Text("‹ Volver") }
        Text("Notificaciones", style = MaterialTheme.typography.headlineMedium)
        Text("Pedidos y promociones de este dispositivo.")
        if (Build.VERSION.SDK_INT >= 33) OutlinedButton(onClick = { permiso.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Activar avisos en el teléfono") }
        if (permisoMensaje.isNotBlank()) Text(permisoMensaje)
        TextButton(onClick = { repo.marcarLeidas(correo); avisos = repo.todas(correo) }) { Text("Marcar todas como leídas") }
        if (avisos.isEmpty()) Text("Aún no tienes notificaciones.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(avisos, key = { it.id }) { a ->
                Card(Modifier.fillMaxWidth().clickable { abierta = a; repo.marcarLeida(correo, a.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text((if (a.leida) "" else "● ") + a.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(a.mensaje, maxLines = 2)
                        Text("Toca para leer", style = MaterialTheme.typography.labelSmall)
                        Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(a.fecha)), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
    abierta?.let { aviso ->
        AlertDialog(onDismissRequest = { abierta = null }, title = { Text(aviso.titulo) },
            text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(aviso.fecha)), style = MaterialTheme.typography.labelMedium)
                Text(aviso.mensaje)
            } }, confirmButton = { TextButton(onClick = { abierta = null }) { Text("Cerrar") } })
    }

}
