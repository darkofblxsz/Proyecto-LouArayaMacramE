package cl.louaraya.macrame.repository

import cl.louaraya.macrame.model.*

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.UUID



fun fechaSolicitudValida(fecha: String): Boolean = try {
    val formato = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    !LocalDate.parse(fecha, formato).isBefore(LocalDate.now())
} catch (_: Exception) { false }

/** Datos del mismo dispositivo. No se envían a Internet ni a otros teléfonos. */
class SolicitudesLocal(private val context: Context) {
    private val prefs = context.getSharedPreferences("lou_araya_local_v1", Context.MODE_PRIVATE)
    private val almacen = AlmacenLocal(context)

    fun todas(): List<SolicitudLocal> {
        val a = JSONArray(prefs.getString("solicitudes_reales_v1", "[]"))
        return (0 until a.length()).map { i ->
            val o = a.getJSONObject(i)
            SolicitudLocal(o.getString("id"), o.getString("correo"), o.getInt("productoId"),
                o.getString("producto"), o.getString("codigo"), o.getInt("unidades"),
                o.getString("fecha"), o.optString("comentario"), o.getString("estado"), o.getLong("creada"), o.optBoolean("personalizada"), o.optString("color", "Original"), o.optString("material", "Original"))
        }.sortedByDescending { it.creada }
    }

    private fun json(lista: List<SolicitudLocal>) = JSONArray().apply {
        lista.forEach { s -> put(JSONObject().put("id", s.id).put("correo", s.correo)
            .put("productoId", s.productoId).put("producto", s.producto).put("codigo", s.codigo)
            .put("unidades", s.unidades).put("fecha", s.fecha).put("comentario", s.comentario)
            .put("personalizada", s.personalizada).put("color", s.color).put("material", s.material)
            .put("estado", s.estado).put("creada", s.creada)) }
    }.toString()

    fun crear(correo: String, productoId: Int, unidades: Int, fecha: String, comentario: String, personalizada: Boolean = false, color: String = "Original", material: String = "Original"): String? {
        if (correo.isBlank() || almacen.correoDeIdentificador(correo) == null) return "Inicia sesión para solicitar."
        val producto = almacen.productos().firstOrNull { it.id == productoId } ?: return "El producto ya no existe."
        if (unidades !in 1..producto.stock) return "La cantidad debe estar entre 1 y ${producto.stock}."
        if (!fechaSolicitudValida(fecha)) return "Ingresa una fecha válida desde hoy (dd/mm/aaaa)."
        if (comentario.length > 500) return "El comentario admite hasta 500 caracteres."
        val nueva = SolicitudLocal(UUID.randomUUID().toString(), correo, producto.id, producto.nombre,
            producto.codigo, unidades, fecha, comentario.trim(), personalizada = personalizada,
            color = if (personalizada) color else "Original", material = if (personalizada) material else "Original")
        val guardado = prefs.edit().putString("solicitudes_reales_v1", json(todas() + nueva)).commit()
        if (guardado) {
            NotificacionesLocal(context).agregar(correo, "Solicitud recibida", "${producto.nombre}: tu solicitud está pendiente.")
            NotificacionesLocal(context).avisarAdmins(context, "Nueva solicitud", "${producto.nombre}: $unidades unidades solicitadas.")
        }
        return if (guardado) null else "No se pudo guardar. Intenta nuevamente."
    }

    fun modificar(id: String, correo: String, unidades: Int, fecha: String, comentario: String,
                  personalizada: Boolean, color: String, material: String): String? {
        val lista = todas()
        val actual = lista.firstOrNull { it.id == id } ?: return "La solicitud no existe."
        if (actual.correo != correo) return "No puedes editar esta solicitud."
        if (actual.estado != "Pendiente") return "Solo puedes editar pedidos pendientes."
        val producto = almacen.productos().firstOrNull { it.id == actual.productoId } ?: return "El producto ya no está disponible."
        if (unidades !in 1..producto.stock) return "Revisa la cantidad; hay ${producto.stock} unidades."
        if (!fechaSolicitudValida(fecha)) return "Selecciona una fecha desde hoy."
        if (comentario.length > 500) return "El comentario admite hasta 500 caracteres."
        val nueva = actual.copy(unidades = unidades, fecha = fecha, comentario = comentario.trim(), personalizada = personalizada,
            color = if (personalizada) color else "Original", material = if (personalizada) material else "Original")
        val ok = prefs.edit().putString("solicitudes_reales_v1", json(lista.map { if (it.id == id) nueva else it })).commit()
        if (ok) NotificacionesLocal(context).avisarAdmins(context, "Pedido modificado", "${actual.producto}: el cliente actualizó su solicitud pendiente.")
        return if (ok) null else "No se pudieron guardar los cambios."
    }

    fun cambiarEstado(id: String, nuevo: String): String? {
        val lista = todas()
        val actual = lista.firstOrNull { it.id == id } ?: return "La solicitud ya no existe."
        val permitidos = when (actual.estado) {
            "Pendiente" -> listOf("Confirmado", "Cancelado")
            "Confirmado" -> listOf("Entregado", "Cancelado")
            else -> emptyList()
        }
        if (nuevo !in permitidos) return "Ese cambio de estado no está permitido."
        val productos = JSONArray(prefs.getString("productos", "[]"))
        val indice = (0 until productos.length()).firstOrNull { productos.getJSONObject(it).getInt("id") == actual.productoId }
        if (nuevo == "Confirmado") {
            if (indice == null) return "El producto fue eliminado; cancela esta solicitud."
            val p = productos.getJSONObject(indice)
            if (p.getInt("stock") < actual.unidades) return "No hay stock suficiente para confirmar."
            p.put("stock", p.getInt("stock") - actual.unidades)
        } else if (actual.estado == "Confirmado" && nuevo == "Cancelado" && indice != null) {
            val p = productos.getJSONObject(indice)
            val stock = p.getInt("stock").toLong() + actual.unidades
            if (stock > Int.MAX_VALUE) return "Revisa el stock del producto antes de cancelar."
            p.put("stock", stock.toInt())
        }
        // Stock y estado se guardan juntos para evitar reservas dobles o parciales.
        val ok = prefs.edit().putString("productos", productos.toString())
            .putString("solicitudes_reales_v1", json(lista.map { if (it.id == id) it.copy(estado = nuevo) else it })).commit()
        if (ok) NotificacionesLocal(context).agregar(actual.correo, "Estado de tu pedido", "${actual.producto}: $nuevo.")
        return if (ok) null else "No se pudo guardar el cambio."
    }
}
