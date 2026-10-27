package cl.louaraya.macrame.repository

import cl.louaraya.macrame.model.*

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale



object PromocionesLocal {
    private const val PREFS = "promociones_lou_araya"
    private const val KEY = "historial"

    fun listar(context: Context): List<OfertaLocal> {
        return try {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "[]") ?: "[]"
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                val fin = if (item.has("finMillis")) {
                    item.optLong("finMillis", 0L)
                } else {
                    // Compatibilidad con las promociones anteriores que solo guardaban fecha.
                    try {
                        val fecha = item.optString("fecha")
                        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))
                        formato.isLenient = false
                        formato.parse("$fecha 23:59")?.time ?: 0L
                    } catch (_: Exception) { 0L }
                }
                OfertaLocal(
                    id = item.optLong("id", index.toLong() + 1L),
                    titulo = item.optString("titulo"),
                    mensaje = item.optString("mensaje"),
                    tipo = item.optString("tipo"),
                    finMillis = fin
                )
            }.reversed()
        } catch (_: Exception) { emptyList() }
    }

    fun guardar(context: Context, oferta: OfertaLocal): Boolean {
        val lista = listar(context).toMutableList()
        lista.add(0, oferta)
        val ok = escribir(context, lista)
        if (ok) {
            val cuentas = JSONArray(context.getSharedPreferences("lou_araya_local_v1", 0).getString("cuentas", "[]"))
            for (i in 0 until cuentas.length()) {
                val c = cuentas.getJSONObject(i)
                if (c.optString("rol") == "USER") NotificacionesLocal(context).agregar(c.getString("correo"), oferta.titulo, oferta.mensaje)
            }
        }
        return ok
    }

    fun eliminar(context: Context, id: Long): Boolean {
        return escribir(context, listar(context).filterNot { it.id == id })
    }

    fun activas(context: Context, ahora: Long = System.currentTimeMillis()): List<OfertaLocal> =
        listar(context).filter { it.finMillis > ahora }

    private fun escribir(context: Context, lista: List<OfertaLocal>): Boolean {
        return try {
            val array = JSONArray()
            // Persistimos en orden cronológico para mantener compatibilidad con historial antiguo.
            lista.asReversed().forEach { oferta ->
                array.put(JSONObject().apply {
                    put("id", oferta.id)
                    put("titulo", oferta.titulo)
                    put("mensaje", oferta.mensaje)
                    put("tipo", oferta.tipo)
                    put("finMillis", oferta.finMillis)
                })
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, array.toString()).commit()
        } catch (_: Exception) { false }
    }
}
