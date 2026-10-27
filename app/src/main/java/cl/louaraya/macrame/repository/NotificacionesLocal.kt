package cl.louaraya.macrame.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class AvisoLocal(val id: String, val correo: String, val titulo: String, val mensaje: String, val fecha: Long, val leida: Boolean)

class NotificacionesLocal(context: Context) {
    private val prefs = context.getSharedPreferences("avisos_lou_araya", Context.MODE_PRIVATE)
    fun todas(correo: String): List<AvisoLocal> {
        val a = JSONArray(prefs.getString("avisos", "[]"))
        return (0 until a.length()).map { a.getJSONObject(it) }.filter { it.optString("correo").equals(correo, true) }
            .map { AvisoLocal(it.getString("id"), it.getString("correo"), it.getString("titulo"), it.getString("mensaje"), it.getLong("fecha"), it.optBoolean("leida")) }.sortedByDescending { it.fecha }
    }
    fun agregar(correo: String, titulo: String, mensaje: String) {
        val a = JSONArray(prefs.getString("avisos", "[]"))
        a.put(JSONObject().put("id", UUID.randomUUID().toString()).put("correo", correo).put("titulo", titulo)
            .put("mensaje", mensaje).put("fecha", System.currentTimeMillis()).put("leida", false))
        prefs.edit().putString("avisos", a.toString()).commit()
    }
    fun marcarLeida(correo: String, id: String) {
        val a = JSONArray(prefs.getString("avisos", "[]"))
        for (i in 0 until a.length()) {
            val aviso = a.getJSONObject(i)
            if (aviso.optString("correo").equals(correo, true) && aviso.optString("id") == id) aviso.put("leida", true)
        }
        prefs.edit().putString("avisos", a.toString()).apply()
    }
    fun marcarLeidas(correo: String) {
        val a = JSONArray(prefs.getString("avisos", "[]"))
        for (i in 0 until a.length()) if (a.getJSONObject(i).optString("correo").equals(correo, true)) a.getJSONObject(i).put("leida", true)
        prefs.edit().putString("avisos", a.toString()).apply()
    }
    fun avisarAdmins(context: Context, titulo: String, mensaje: String) {
        val cuentas = JSONArray(context.getSharedPreferences("lou_araya_local_v1", 0).getString("cuentas", "[]"))
        for (i in 0 until cuentas.length()) {
            val c = cuentas.getJSONObject(i)
            if (c.optString("rol") == "ADMIN") agregar(c.getString("correo"), titulo, mensaje)
        }
    }
}
