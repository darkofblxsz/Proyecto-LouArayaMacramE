
package cl.louaraya.macrame

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.louaraya.macrame.ui.theme.LouArayaMacrameTheme

private val VerdeOscuro = Color(0xFF123D35)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            LouArayaMacrameTheme {

                val context = LocalContext.current

                val almacen = remember {
                    AlmacenLocal(applicationContext)
                }

                // ========================================
                // NAVEGACIÓN PRINCIPAL
                // ========================================

                var pantallaActual by rememberSaveable {
                    mutableStateOf("bienvenida")
                }

                // ========================================
                // DATOS DEL REGISTRO
                // ========================================

                var correoSesion by rememberSaveable { mutableStateOf<String?>(null) }

                var correo by rememberSaveable {
                    mutableStateOf("")
                }

                var confirmarCorreo by rememberSaveable {
                    mutableStateOf("")
                }

                var contrasena by rememberSaveable {
                    mutableStateOf("")
                }

                var confirmarContrasena by rememberSaveable {
                    mutableStateOf("")
                }

                var nombre by rememberSaveable {
                    mutableStateOf("")
                }

                var apellido by rememberSaveable {
                    mutableStateOf("")
                }

                var fechaNacimiento by rememberSaveable {
                    mutableStateOf("")
                }

                var usuario by rememberSaveable {
                    mutableStateOf("")
                }

                var codigoPais by rememberSaveable {
                    mutableStateOf("+56")
                }

                var telefono by rememberSaveable {
                    mutableStateOf("")
                }

                var aceptaTerminos by rememberSaveable {
                    mutableStateOf(false)
                }

                // ========================================
                // LIMPIAR FORMULARIO
                // ========================================

                fun limpiarRegistro() {
                    correo = ""
                    confirmarCorreo = ""
                    contrasena = ""
                    confirmarContrasena = ""
                    nombre = ""
                    apellido = ""
                    fechaNacimiento = ""
                    usuario = ""
                    codigoPais = "+56"
                    telefono = ""
                    aceptaTerminos = false
                }

                // ========================================
                // CERRAR SESIÓN
                // ========================================

                fun cerrarSesion() {
                    correoSesion = null
                    pantallaActual = "login"

                    Toast.makeText(
                        context,
                        "Sesión cerrada",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // ========================================
                // BOTÓN ATRÁS DEL TELÉFONO
                // ========================================

                BackHandler(
                    enabled = pantallaActual != "bienvenida"
                ) {

                    pantallaActual = when (pantallaActual) {

                        "terminos" -> "registro"

                        "registro" -> "login"

                        "login" -> "bienvenida"

                        "admin" -> "login"

                        "adminCampanas" -> "admin"

                        "adminProductos" -> "admin"

                        "adminSolicitudes" -> "admin"

                        "misSolicitudes" -> "catalogo"

                        "catalogo" -> "bienvenida"

                        "usuario" -> "login"

                        else -> "bienvenida"
                    }
                }

                // ========================================
                // CONTROL DE PANTALLAS
                // ========================================

                when (pantallaActual) {

                    // ====================================
                    // PANTALLA DE BIENVENIDA
                    // ====================================

                    "bienvenida" -> {

                        PantallaBienvenida(
                            onComenzar = {
                                // El cliente puede explorar el catálogo sin iniciar sesión.
                                pantallaActual = "catalogo"
                            },
                            onIniciarSesion = {
                                pantallaActual = "login"
                            }
                        )
                    }

                    // ====================================
                    // INICIO DE SESIÓN
                    // ====================================

                    "login" -> {

                        PantallaLogin(

                            onIngresar = {
                                    identificador,
                                    clave ->

                                val rol = almacen.autenticar(
                                    identificador,
                                    clave
                                )

                                when (rol) {

                                    "ADMIN" -> {
                                        correoSesion = almacen.correoDeIdentificador(identificador)

                                        Toast.makeText(
                                            context,
                                            "Bienvenido administrador",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        pantallaActual = "admin"
                                    }

                                    "USER" -> {
                                        correoSesion = almacen.correoDeIdentificador(identificador)

                                        Toast.makeText(
                                            context,
                                            "Inicio de sesión correcto",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        pantallaActual = "catalogo"
                                    }

                                    else -> {

                                        Toast.makeText(
                                            context,
                                            "Correo, usuario o contraseña incorrectos",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },

                            onRegistrarse = {
                                pantallaActual = "registro"
                            },

                            onVolver = {
                                pantallaActual = "bienvenida"
                            }
                        )
                    }

                    // ====================================
                    // REGISTRO DE USUARIOS
                    // ====================================

                    "registro" -> {

                        PantallaRegistro(

                            correo = correo,

                            onCorreoChange = {
                                correo = it
                            },

                            confirmarCorreo = confirmarCorreo,

                            onConfirmarCorreoChange = {
                                confirmarCorreo = it
                            },

                            contrasena = contrasena,

                            onContrasenaChange = {
                                contrasena = it
                            },

                            confirmarContrasena = confirmarContrasena,

                            onConfirmarContrasenaChange = {
                                confirmarContrasena = it
                            },

                            nombre = nombre,

                            onNombreChange = {
                                nombre = it
                            },

                            apellido = apellido,

                            onApellidoChange = {
                                apellido = it
                            },

                            fechaNacimiento = fechaNacimiento,

                            onFechaNacimientoChange = {
                                fechaNacimiento = it
                            },

                            usuario = usuario,

                            onUsuarioChange = {
                                usuario = it
                            },

                            codigoPais = codigoPais,

                            onCodigoPaisChange = {
                                codigoPais = it
                            },

                            telefono = telefono,

                            onTelefonoChange = {
                                telefono = it
                            },

                            aceptaTerminos = aceptaTerminos,

                            onAceptaTerminosChange = {
                                aceptaTerminos = it
                            },

                            onVolver = {
                                pantallaActual = "login"
                            },

                            onLeerTerminos = {
                                pantallaActual = "terminos"
                            },

                            // ============================
                            // CREAR CUENTA REAL
                            // ============================

                            onCrearCuenta = {

                                when {

                                    correo.trim().isEmpty() -> {

                                        Toast.makeText(
                                            context,
                                            "Ingresa tu correo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    !correo.trim().equals(
                                        confirmarCorreo.trim(),
                                        ignoreCase = true
                                    ) -> {

                                        Toast.makeText(
                                            context,
                                            "Los correos no coinciden",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    contrasena !=
                                            confirmarContrasena -> {

                                        Toast.makeText(
                                            context,
                                            "Las contraseñas no coinciden",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    !aceptaTerminos -> {

                                        Toast.makeText(
                                            context,
                                            "Debes aceptar los términos y condiciones",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    else -> {

                                        val resultado =
                                            almacen.registrar(
                                                email = correo,
                                                password = contrasena,
                                                nombre = nombre,
                                                usuario = usuario,
                                                apellido = apellido,
                                                fechaNacimiento = fechaNacimiento,
                                                telefono = codigoPais + telefono
                                            )

                                        if (resultado == null) {

                                            Toast.makeText(
                                                context,
                                                "Cuenta creada correctamente. Ahora inicia sesión.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            limpiarRegistro()

                                            pantallaActual = "login"

                                        } else {

                                            Toast.makeText(
                                                context,
                                                resultado,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // ====================================
                    // TÉRMINOS Y CONDICIONES
                    // ====================================

                    "terminos" -> {

                        PantallaTerminos(
                            onVolver = {
                                pantallaActual = "registro"
                            }
                        )
                    }

                    // ====================================
                    // DASHBOARD DEL ADMINISTRADOR
                    // ====================================

                    "admin" -> {
                        PantallaDashboardAdmin(
                            cantidadProductos = almacen.productos().size,
                            productosDisponibles = almacen.productos().count { it.stock > 0 },
                            onGestionarProductos = {
                                pantallaActual = "adminProductos"
                            },
                            onGestionarCampanas = {
                                pantallaActual = "adminCampanas"
                            },
                            onProximamente = { seccion ->
                                if (seccion == "Gestión de solicitudes") {
                                    pantallaActual = "adminSolicitudes"
                                } else Toast.makeText(
                                    context,
                                    "$seccion se integrará en el siguiente módulo",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onCerrarSesion = {
                                cerrarSesion()
                            }
                        )
                    }

                    // ====================================
                    // GESTIÓN DE PRODUCTOS
                    // ====================================

                    "adminProductos" -> {
                        PantallaProductosAdmin(
                            almacen = almacen,
                            onVolver = {
                                pantallaActual = "admin"
                            }
                        )
                    }

                    "adminSolicitudes" -> {
                        PantallaSolicitudes(onVolver = { pantallaActual = "admin" }, administrador = true)
                    }

                    // ====================================
                    // GESTIÓN DE CAMPAÑAS (YA EXISTENTE)
                    // ====================================

                    "adminCampanas" -> {
                        PantallaCampanas(
                            almacen = almacen,
                            onSalir = {
                                pantallaActual = "admin"
                            }
                        )
                    }

                    // ====================================
                    // CATÁLOGO DE CLIENTE
                    // ====================================

                    "misSolicitudes" -> {
                        PantallaSolicitudes(onVolver = { pantallaActual = "catalogo" }, correoCliente = correoSesion)
                    }

                    "catalogo", "usuario" -> {
                        PantallaCatalogo(
                            almacen = almacen,
                            onInicio = {
                                pantallaActual = "bienvenida"
                            },
                            correoCliente = correoSesion,
                            onSolicitudes = {
                                pantallaActual = if (correoSesion != null) "misSolicitudes" else "login"
                            },
                            onPerfil = {
                                cerrarSesion()
                            }
                        )
                    }
                }
            }
        }
    }
}

// ========================================
// PANTALLA DE BIENVENIDA ORIGINAL
// ========================================

@Composable
fun PantallaBienvenida(
    onComenzar: () -> Unit,
    onIniciarSesion: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(
                R.drawable.fondo_bienvenida
            ),
            contentDescription = "Decoración artesanal de macramé",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.38f)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 28.dp,
                    vertical = 40.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "✦",
                    fontSize = 64.sp,
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "LOU ARAYA",
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp,
                    color = Color.White
                )

                Text(
                    text = "M A C R A M É",
                    fontSize = 14.sp,
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(55.dp)
                )

                Text(
                    text = "Diseños que inspiran\ndecoración única",
                    fontSize = 23.sp,
                    lineHeight = 31.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        onComenzar()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = VerdeOscuro
                    )
                ) {

                    Text(
                        text = "Comenzar",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        fontSize = 13.sp,
                        color = Color.White.copy(
                            alpha = 0.85f
                        )
                    )

                    Text(
                        text = "Iniciar sesión",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            onIniciarSesion()
                        }
                    )
                }
            }
        }
    }
}
