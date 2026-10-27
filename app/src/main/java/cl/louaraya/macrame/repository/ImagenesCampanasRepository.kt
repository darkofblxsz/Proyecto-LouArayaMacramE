package cl.louaraya.macrame.repository

import android.content.Context

/** Conserva el mismo archivo y clave usados por las campañas existentes. */
class ImagenesCampanasRepository(context: Context) {
    private val prefs = context.getSharedPreferences("imagenes_campanas", Context.MODE_PRIVATE)
    fun leer(): String = prefs.getString("imagenes", "{}") ?: "{}"
    fun guardar(json: String) { prefs.edit().putString("imagenes", json).apply() }
}
