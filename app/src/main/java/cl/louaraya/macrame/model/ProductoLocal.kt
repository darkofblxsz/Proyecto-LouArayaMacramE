package cl.louaraya.macrame.model

data class ProductoLocal(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val categoria: String,
    val material: String,
    val dimensiones: String,
    val precio: Int,
    val stock: Int,
    val imagenKey: String,
    val fotos: List<String> = emptyList()
)
