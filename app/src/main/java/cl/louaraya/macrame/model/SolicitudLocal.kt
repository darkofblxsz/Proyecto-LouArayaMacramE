package cl.louaraya.macrame.model

data class SolicitudLocal(
    val id: String, val correo: String, val productoId: Int, val producto: String,
    val codigo: String, val unidades: Int, val fecha: String, val comentario: String,
    val estado: String = "Pendiente", val creada: Long = System.currentTimeMillis(),
    val personalizada: Boolean = false, val color: String = "Original", val material: String = "Original"
)
