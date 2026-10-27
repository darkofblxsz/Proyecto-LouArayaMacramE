package cl.louaraya.macrame.model

data class OfertaLocal(
    val id: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val finMillis: Long
)
