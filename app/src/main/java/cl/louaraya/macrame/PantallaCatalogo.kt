package cl.louaraya.macrame

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val CatalogoFondo = Color(0xFFFAF7F2)
private val CatalogoVerde = Color(0xFF123D35)
private val CatalogoTerracota = Color(0xFF9C6248)
private val CatalogoTexto = Color(0xFF342D29)

private val categoriasCatalogo = listOf("Todas", "Matrimonios", "Festivales", "Hogar", "Viajes")

@Composable
fun PantallaCatalogo(
    almacen: AlmacenLocal,
    onInicio: () -> Unit,
    onPerfil: () -> Unit,
    correoCliente: String?,
    onSolicitudes: () -> Unit
) {
    val context = LocalContext.current
    var solicitar by remember { mutableStateOf<ProductoLocal?>(null) }
    var productoQr by remember { mutableStateOf<ProductoLocal?>(null) }
    val productosCatalogo = almacen.productos().sortedBy { it.id }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf("Todas") }
    var soloFavoritos by rememberSaveable { mutableStateOf(false) }
    var favoritos by rememberSaveable { mutableStateOf(setOf<Int>()) }
    var productoDetalle by remember { mutableStateOf<ProductoLocal?>(null) }

    val visibles = productosCatalogo.filter { producto ->
        val categoriaOk = categoriaSeleccionada == "Todas" || producto.categoria == categoriaSeleccionada
        val textoOk = producto.nombre.contains(busqueda, ignoreCase = true) ||
            producto.material.contains(busqueda, ignoreCase = true) ||
            producto.codigo.contains(busqueda, ignoreCase = true)
        categoriaOk && textoOk && (!soloFavoritos || producto.id in favoritos)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CatalogoFondo)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CatalogoVerde),
                    contentAlignment = Alignment.Center
                ) {
                    Text("LA", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("LOU ARAYA", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = CatalogoTexto)
                    Text("M A C R A M É", fontSize = 10.sp, color = Color.Gray)
                }
                Text(
                    text = if (soloFavoritos) "♥" else "♡",
                    fontSize = 29.sp,
                    color = CatalogoTerracota,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { soloFavoritos = !soloFavoritos }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(Modifier.height(23.dp))
            Text(
                if (soloFavoritos) "Mis favoritos" else "Explora el catálogo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CatalogoTexto
            )
            Spacer(Modifier.height(4.dp))
            Text("Piezas hechas a mano para momentos especiales.", color = Color.Gray, fontSize = 13.sp)

            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Buscar productos o materiales", fontSize = 13.sp) },
                leadingIcon = { Text("⌕", fontSize = 25.sp, color = CatalogoVerde) },
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CatalogoVerde,
                    unfocusedBorderColor = Color(0xFFE2D9CF),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            BotonLectorQr { valor ->
                val codigo = codigoDesdeQr(valor)
                val encontrado = productosCatalogo.firstOrNull { it.codigo == codigo }
                if (encontrado != null) productoDetalle = encontrado
                else Toast.makeText(context, "El QR no corresponde a un producto del catálogo", Toast.LENGTH_LONG).show()
            }
            Spacer(Modifier.height(17.dp))
            Text("Categorías", fontWeight = FontWeight.SemiBold, color = CatalogoTexto, fontSize = 17.sp)
            Spacer(Modifier.height(7.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                rowItems(categoriasCatalogo) { categoria ->
                    val seleccionada = categoriaSeleccionada == categoria
                    FilterChip(
                        selected = seleccionada,
                        onClick = {
                            categoriaSeleccionada = categoria
                            soloFavoritos = false
                        },
                        label = { Text(categoria, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CatalogoVerde,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = CatalogoTexto
                        )
                    )
                }
            }

            Spacer(Modifier.height(11.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nuestros diseños", fontWeight = FontWeight.Bold, color = CatalogoTexto, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                Text("${visibles.size} productos", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (visibles.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No encontramos productos", fontWeight = FontWeight.SemiBold, color = CatalogoTexto)
                    Text("Prueba otra búsqueda o categoría.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gridItems(visibles, key = { it.id }) { producto ->
                    TarjetaProductoCatalogo(
                        producto = producto,
                        favorito = producto.id in favoritos,
                        onFavorito = {
                            favoritos = if (producto.id in favoritos) favoritos - producto.id else favoritos + producto.id
                        },
                        onAbrir = { productoDetalle = producto }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemNavegacionCatalogo("⌂", "Inicio", false, onInicio)
            ItemNavegacionCatalogo("▦", "Catálogo", true) {
                categoriaSeleccionada = "Todas"
                busqueda = ""
                soloFavoritos = false
            }
            ItemNavegacionCatalogo("♡", "Favoritos", soloFavoritos) {
                soloFavoritos = !soloFavoritos
                if (soloFavoritos) categoriaSeleccionada = "Todas"
            }
            ItemNavegacionCatalogo("☷", "Solicitudes", false, onSolicitudes)
            ItemNavegacionCatalogo("♙", "Sesión", false, onPerfil)
        }
    }

    productoQr?.let { DialogoQr(it, onCerrar = { productoQr = null }) }
    solicitar?.let { producto ->
        if (correoCliente != null) FormularioSolicitud(producto, correoCliente,
            onCerrar = { solicitar = null }, onCreada = {
                solicitar = null
                Toast.makeText(context, "Solicitud creada correctamente", Toast.LENGTH_LONG).show()
                onSolicitudes()
            })
    }

    // Emergente independiente: no empuja ni tapa permanentemente el catálogo.
    BannerOfertaCatalogo(onVerOferta = { tipo ->
        busqueda = ""
        soloFavoritos = false
        categoriaSeleccionada = if (tipo in categoriasCatalogo) tipo else "Todas"
    })

    productoDetalle?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoDetalle = null },
            containerColor = CatalogoFondo,
            title = { Text(producto.nombre, color = CatalogoTexto, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    FotoProducto(producto.fotos.firstOrNull(), producto.imagenKey, producto.nombre,
                        Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)))
                    if (producto.fotos.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems(producto.fotos.drop(1)) { foto ->
                            FotoProducto(foto, producto.imagenKey, producto.nombre, Modifier.size(140.dp))
                        }
                    }
                    Text("Stock disponible: ${producto.stock}")
                    TextButton(onClick = { productoQr = producto; productoDetalle = null }) { Text("Ver QR del producto") }
                    Text("Código: ${producto.codigo}")
                    Text("Categoría: ${producto.categoria}")
                    Text("Material: ${producto.material}")
                    Text("Dimensiones: ${producto.dimensiones}")
                    Text("Precio: ${formatearPrecioCatalogo(producto.precio)}", fontWeight = FontWeight.Bold, color = CatalogoTerracota)
                }
            },
            confirmButton = {
                Button(enabled = producto.stock > 0, onClick = {
                    productoDetalle = null
                    if (correoCliente == null) {
                        Toast.makeText(context, "Inicia sesión para crear solicitudes", Toast.LENGTH_LONG).show()
                        onPerfil()
                    } else solicitar = producto
                }) { Text(if (producto.stock > 0) "Solicitar" else "Sin stock") }
            },
            dismissButton = {
                TextButton(onClick = { productoDetalle = null }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
private fun TarjetaProductoCatalogo(
    producto: ProductoLocal,
    favorito: Boolean,
    onFavorito: () -> Unit,
    onAbrir: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrir),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(9.dp)) {
            Box {
                FotoProducto(producto.fotos.firstOrNull(), producto.imagenKey, producto.nombre,
                    Modifier.fillMaxWidth().height(122.dp).clip(RoundedCornerShape(10.dp)))
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(5.dp)
                        .size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.95f))
                        .clickable(onClick = onFavorito),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (favorito) "♥" else "♡", color = CatalogoTerracota, fontSize = 21.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(producto.nombre, fontWeight = FontWeight.SemiBold, color = CatalogoTexto, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
            Text(producto.material, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(formatearPrecioCatalogo(producto.precio), color = CatalogoVerde, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ItemNavegacionCatalogo(
    simbolo: String,
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 5.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(simbolo, fontSize = 21.sp, color = if (seleccionado) CatalogoVerde else Color.Gray)
        Text(texto, fontSize = 10.sp, color = if (seleccionado) CatalogoVerde else Color.Gray, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
    }
}

private fun imagenProductoCatalogo(key: String): Int = when (key.lowercase()) {
    "matrimonios" -> R.drawable.matrimonios
    "festivales" -> R.drawable.festivales
    "viajes" -> R.drawable.viajes
    else -> R.drawable.hogar
}

private fun formatearPrecioCatalogo(precio: Int): String = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
    maximumFractionDigits = 0
    minimumFractionDigits = 0
}.format(precio)
