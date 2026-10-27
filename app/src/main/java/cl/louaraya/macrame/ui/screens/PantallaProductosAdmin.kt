package cl.louaraya.macrame.ui.screens

import cl.louaraya.macrame.R
import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.viewmodel.*
import cl.louaraya.macrame.ui.components.*

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val ProductosAdminFondo = Color(0xFFFAF7F2)
private val ProductosAdminVerde = Color(0xFF123D35)
private val ProductosAdminTerracota = Color(0xFF9C6248)
private val ProductosAdminTexto = Color(0xFF25342F)
private val categoriasProducto = listOf("Matrimonios", "Festivales", "Hogar", "Viajes")

@Composable
fun PantallaProductosAdmin(
    almacen: TiendaViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var fotos by rememberSaveable { mutableStateOf(listOf<String>()) }
    var procesandoFotos by remember { mutableStateOf(false) }
    var productoQr by remember { mutableStateOf<ProductoLocal?>(null) }
    var productos by remember { mutableStateOf(almacen.productos()) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    var idEditando by rememberSaveable { mutableStateOf<Int?>(null) }
    var productoEliminar by remember { mutableStateOf<ProductoLocal?>(null) }

    var nombre by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("Hogar") }
    var material by rememberSaveable { mutableStateOf("") }
    var dimensiones by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var errorFormulario by remember { mutableStateOf("") }
    var menuCategoria by remember { mutableStateOf(false) }

    fun refrescar() {
        productos = almacen.productos().sortedBy { it.id }
    }

    fun abrirNuevo() {
        idEditando = null
        fotos = emptyList()
        nombre = ""
        categoria = "Hogar"
        material = "Algodón natural"
        dimensiones = ""
        precio = ""
        stock = "0"
        errorFormulario = ""
        mostrarFormulario = true
    }

    fun abrirEdicion(producto: ProductoLocal) {
        idEditando = producto.id
        fotos = producto.fotos
        nombre = producto.nombre
        categoria = producto.categoria
        material = producto.material
        dimensiones = producto.dimensiones
        precio = producto.precio.toString()
        stock = producto.stock.toString()
        errorFormulario = ""
        mostrarFormulario = true
    }

    val visibles = productos.filter { producto ->
        val coincideTexto = producto.nombre.contains(busqueda, true) ||
            producto.codigo.contains(busqueda, true) ||
            producto.material.contains(busqueda, true)
        val coincideFiltro = when (filtro) {
            "Disponibles" -> producto.stock > 0
            "Stock bajo" -> producto.stock in 0..3
            else -> true
        }
        coincideTexto && coincideFiltro
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProductosAdminFondo)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "‹",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onVolver)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                fontSize = 34.sp,
                color = ProductosAdminVerde
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Productos", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = ProductosAdminTexto)
                Text("Gestión de catálogo", fontSize = 12.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ProductosAdminVerde)
                    .clickable(onClick = ::abrirNuevo),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 29.sp)
            }
        }

        BotonLectorQr { valor ->
            val codigo = codigoDesdeQr(valor)
            val encontrado = productos.firstOrNull { it.codigo == codigo }
            if (encontrado != null) abrirEdicion(encontrado)
            else Toast.makeText(context, "El QR no corresponde a un producto del catálogo", Toast.LENGTH_LONG).show()
        }
        Spacer(Modifier.height(17.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ResumenProductosAdmin(
                modifier = Modifier.weight(1f),
                valor = productos.size.toString(),
                titulo = "Productos",
                color = Color(0xFFF0DDD1)
            )
            ResumenProductosAdmin(
                modifier = Modifier.weight(1f),
                valor = productos.count { it.stock > 0 }.toString(),
                titulo = "Disponibles",
                color = Color(0xFFDDE8DC)
            )
            ResumenProductosAdmin(
                modifier = Modifier.weight(1f),
                valor = productos.count { it.stock in 0..3 }.toString(),
                titulo = "Stock bajo",
                color = Color(0xFFF5E8BF)
            )
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Buscar producto...") },
            leadingIcon = { Text("⌕", fontSize = 24.sp, color = ProductosAdminVerde) },
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Todos", "Disponibles", "Stock bajo").forEach { opcion ->
                FilterChip(
                    selected = filtro == opcion,
                    onClick = { filtro = opcion },
                    label = { Text(opcion, fontSize = 12.sp) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Catálogo", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = ProductosAdminTexto)
            Spacer(Modifier.weight(1f))
            Text("${visibles.size} productos", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(7.dp))

        if (visibles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay productos que coincidan con la búsqueda.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(visibles, key = { it.id }) { producto ->
                    TarjetaProductoAdmin(
                        producto = producto,
                        onEditar = { abrirEdicion(producto) },
                        onEliminar = { productoEliminar = producto },
                        onQr = { productoQr = producto }
                    )
                }
            }
        }
    }

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = { if (!procesandoFotos) mostrarFormulario = false },
            containerColor = Color.White,
            title = {
                Text(
                    if (idEditando == null) "Agregar producto" else "Editar producto",
                    color = ProductosAdminTexto,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; errorFormulario = "" },
                        label = { Text("Nombre del producto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box {
                        OutlinedButton(
                            onClick = { menuCategoria = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Categoría: $categoria", color = ProductosAdminVerde)
                        }
                        DropdownMenu(
                            expanded = menuCategoria,
                            onDismissRequest = { menuCategoria = false }
                        ) {
                            categoriasProducto.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        categoria = opcion
                                        menuCategoria = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = material,
                        onValueChange = { material = it },
                        label = { Text("Material") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dimensiones,
                        onValueChange = { dimensiones = it },
                        label = { Text("Dimensiones (ej. 80 × 120 cm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it.filter { caracter -> caracter.isDigit() }; errorFormulario = "" },
                        label = { Text("Precio en pesos chilenos") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it.filter { caracter -> caracter.isDigit() }; errorFormulario = "" },
                        label = { Text("Unidades en stock") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorFormulario.isNotBlank()) {
                        Text(errorFormulario, color = Color(0xFFB3261E), fontSize = 12.sp)
                    }
                    EditorFotos(fotos, categoria, onCambiar = { fotos = it },
                        onOcupado = { procesandoFotos = it })
                }
            },
            confirmButton = {
                Button(
                    enabled = !procesandoFotos,
                    onClick = {
                        val precioNumero = precio.toIntOrNull()
                        val stockNumero = stock.toIntOrNull()
                        when {
                            nombre.isBlank() -> errorFormulario = "Escribe el nombre del producto."
                            material.isBlank() -> errorFormulario = "Escribe el material del producto."
                            dimensiones.isBlank() -> errorFormulario = "Escribe las dimensiones."
                            precioNumero == null || precioNumero <= 0 -> errorFormulario = "Ingresa un precio mayor a cero."
                            stockNumero == null -> errorFormulario = "Ingresa una cantidad de stock válida."
                            else -> {
                                val id = idEditando ?: almacen.siguienteIdProducto()
                                val codigo = productos.firstOrNull { it.id == id }?.codigo
                                    ?: "MAC-${id.toString().padStart(3, '0')}"
                                val nuevo = ProductoLocal(
                                    id = id,
                                    codigo = codigo,
                                    nombre = nombre.trim(),
                                    categoria = categoria,
                                    material = material.trim(),
                                    dimensiones = dimensiones.trim(),
                                    precio = precioNumero,
                                    stock = stockNumero,
                                    imagenKey = categoria.lowercase(),
                                    fotos = fotos.toList()
                                )
                                almacen.guardarProducto(nuevo)
                                refrescar()
                                mostrarFormulario = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProductosAdminVerde)
                ) { Text(if (idEditando == null) "Agregar" else "Guardar") }
            },
            dismissButton = {
                TextButton(enabled = !procesandoFotos, onClick = { mostrarFormulario = false }) {
                    Text("Cancelar", color = ProductosAdminTerracota)
                }
            }
        )
    }

    productoQr?.let { DialogoQr(it, onCerrar = { productoQr = null }) }

    productoEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Quieres eliminar ${producto.nombre} del catálogo?") },
            confirmButton = {
                Button(
                    onClick = {
                        almacen.eliminarProducto(producto.id)
                        refrescar()
                        productoEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { productoEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ResumenProductosAdmin(
    modifier: Modifier,
    valor: String,
    titulo: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 13.dp)) {
            Text(valor, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = ProductosAdminVerde)
            Text(titulo, fontSize = 11.sp, color = ProductosAdminTexto, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TarjetaProductoAdmin(
    producto: ProductoLocal,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onQr: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FotoProducto(producto.fotos.firstOrNull(), producto.imagenKey, producto.nombre,
                Modifier.size(width = 76.dp, height = 82.dp).clip(RoundedCornerShape(11.dp)))
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, color = ProductosAdminTexto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(producto.codigo, color = Color.Gray, fontSize = 10.sp)
                Spacer(Modifier.height(3.dp))
                Text(formatearPrecioAdmin(producto.precio), color = ProductosAdminVerde, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Stock: ${producto.stock}", color = if (producto.stock <= 3) ProductosAdminTerracota else Color.Gray, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = onEditar, contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp)) {
                    Text("Editar", color = ProductosAdminVerde, fontSize = 12.sp)
                }
                TextButton(onClick = onQr) { Text("Ver QR") }
                TextButton(onClick = onEliminar, contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp)) {
                    Text("Eliminar", color = Color(0xFFB3261E), fontSize = 12.sp)
                }
            }
        }
    }
}

private fun imagenProducto(key: String): Int = when (key.lowercase()) {
    "matrimonios" -> R.drawable.matrimonios
    "festivales" -> R.drawable.festivales
    "viajes" -> R.drawable.viajes
    else -> R.drawable.hogar
}

private fun formatearPrecioAdmin(precio: Int): String = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}.format(precio)
