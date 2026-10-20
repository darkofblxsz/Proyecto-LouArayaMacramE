
package cl.louaraya.macrame

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable

private val AdminVerde = Color(0xFF123D35)
private val AdminFondo = Color(0xFFFAF7F2)
private val AdminTerracota = Color(0xFF9C6248)
private val AdminTexto = Color(0xFF25342F)

@Composable
fun PantallaDashboardAdmin(
    cantidadProductos: Int,
    productosDisponibles: Int,
    onGestionarProductos: () -> Unit,
    onGestionarCampanas: () -> Unit,
    onProximamente: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {

    var mostrarPromocion by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler(enabled = mostrarPromocion) {
        mostrarPromocion = false
    }

    if (mostrarPromocion) {
        PantallaEnviarPromocion(
            onVolver = {
                mostrarPromocion = false
            }
        )
    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminFondo)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(AdminVerde),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "LA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(11.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "LOU ARAYA",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = AdminTexto
                    )

                    Text(
                        "Panel de administración",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                TextButton(
                    onClick = onCerrarSesion
                ) {
                    Text(
                        "Salir",
                        color = AdminVerde
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "¡Bienvenida!",
                style = MaterialTheme.typography.headlineMedium,
                color = AdminTexto,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                "Aquí tienes el resumen de tu tienda.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ResumenAdmin(
                    modifier = Modifier.weight(1f),
                    cantidad = cantidadProductos.toString(),
                    titulo = "Productos",
                    detalle = "En catálogo",
                    fondo = Color(0xFFF1DDD0)
                )

                ResumenAdmin(
                    modifier = Modifier.weight(1f),
                    cantidad = productosDisponibles.toString(),
                    titulo = "Disponibles",
                    detalle = "Con stock",
                    fondo = Color(0xFFDCE7DC)
                )
            }

            Spacer(Modifier.height(25.dp))

            Text(
                "Accesos rápidos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AdminTexto
            )

            Spacer(Modifier.height(11.dp))

            AccionDashboard(
                simbolo = "▦",
                titulo = "Gestión de productos",
                descripcion = "Agregar, editar y revisar el catálogo",
                onClick = onGestionarProductos
            )

            AccionDashboard(
                simbolo = "▤",
                titulo = "Ver solicitudes",
                descripcion = "Revisar pedidos y actualizar sus estados",
                onClick = {
                    onProximamente("Gestión de solicitudes")
                }
            )

            AccionDashboard(
                simbolo = "✿",
                titulo = "Gestionar campañas",
                descripcion = "Crear y administrar colecciones",
                onClick = onGestionarCampanas
            )

            AccionDashboard(
                simbolo = "✉",
                titulo = "Enviar promoción",
                descripcion = "Crear ofertas y preparar notificaciones",
                onClick = {
                    mostrarPromocion = true
                }
            )

            Spacer(Modifier.height(18.dp))

            Text(
                "Los productos y el stock se guardan localmente en este dispositivo. Las promociones se guardan como borradores hasta implementar el envío push.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ResumenAdmin(
    modifier: Modifier = Modifier,
    cantidad: String,
    titulo: String,
    detalle: String,
    fondo: Color
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondo
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {

            Text(
                cantidad,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = AdminVerde
            )

            Text(
                titulo,
                fontWeight = FontWeight.SemiBold,
                color = AdminTexto,
                fontSize = 14.sp
            )

            Text(
                detalle,
                color = AdminTexto.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AccionDashboard(
    simbolo: String,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 13.dp,
                    vertical = 15.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8EEE8)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    simbolo,
                    color = AdminVerde,
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    titulo,
                    color = AdminTexto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    descripcion,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Text(
                "›",
                color = AdminTerracota,
                fontSize = 28.sp
            )
        }
    }
}
