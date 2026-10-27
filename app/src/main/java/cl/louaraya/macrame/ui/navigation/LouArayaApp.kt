package cl.louaraya.macrame.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import cl.louaraya.macrame.repository.NotificacionesLocal
import cl.louaraya.macrame.repository.SolicitudesLocal
import androidx.compose.ui.platform.LocalContext
import cl.louaraya.macrame.ui.screens.*
import cl.louaraya.macrame.viewmodel.TiendaViewModel



@Composable
fun LouArayaApp(viewModel: TiendaViewModel) {
    with(viewModel) {
                val context = LocalContext.current

                val almacen = viewModel
                ObservarAvisos(correoSesion)
                LaunchedEffect(avisoPendiente, correoSesion) {
                    if (avisoPendiente != null) {
                        if (correoSesion != null && correoSesion.equals(correoAvisoPendiente, true)) {
                            avisoDetalle = avisoPendiente
                            avisoPendiente = null
                            pantallaActual = "notificaciones"
                        } else {
                            if (correoSesion != null) correoSesion = null
                            pantallaActual = "login"
                            Toast.makeText(context, "Inicia sesión con la cuenta que recibió este aviso", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                fun navegar(ruta: String) {
                    avisoDetalle = null
                    pantallaActual = if (correoSesion == null && ruta in listOf("perfil", "misSolicitudes", "notificaciones")) "login" else ruta
                }
                fun volverInicio() { pantallaActual = if (perfil()?.optString("rol") == "ADMIN") "admin" else "inicio" }

                // ========================================
                // CERRAR SESIÓN
                // ========================================

                fun cerrarSesion() {
                    viewModel.cerrarSesion()

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

                        "perfil", "notificaciones" -> if (perfil()?.optString("rol") == "ADMIN") "admin" else "inicio"
                        "inicio" -> "bienvenida"
                        "favoritos" -> "inicio"
                        "admin" -> "bienvenida"

                        "adminCuentas", "crearAdmin" -> "admin"
                        "adminCampanas" -> "admin"

                        "adminProductos" -> "admin"

                        "adminSolicitudes" -> "admin"

                        "mapaFerias" -> if (perfil()?.optString("rol") == "ADMIN") "admin" else "inicio"

                        "misSolicitudes" -> "inicio"

                        "catalogo" -> "inicio"

                        "usuario" -> "login"

                        else -> "bienvenida"
                    }
                }

                // ========================================
                // CONTROL DE PANTALLAS
                // ========================================

                val esAdmin = perfil()?.optString("rol") == "ADMIN"
                val conMenu = !esAdmin && pantallaActual in listOf("inicio", "usuario", "catalogo", "favoritos", "mapaFerias", "misSolicitudes", "perfil", "notificaciones")
                Column(Modifier.fillMaxSize().then(if (conMenu) Modifier.statusBarsPadding() else Modifier)) {
                    if (conMenu) CabeceraTienda(viewModel, ::navegar)
                    Box(Modifier.weight(1f).then(if (conMenu) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier)) {
                    key(pantallaActual) {
                when (pantallaActual) {

                    // ====================================
                    // PANTALLA DE BIENVENIDA
                    // ====================================

                    "bienvenida" -> {

                        PantallaBienvenida(
                            onComenzar = {
                                // El cliente puede explorar el catálogo sin iniciar sesión.
                                pantallaActual = "inicio"
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

                            onIngresar = { identificador, clave ->
                                Toast.makeText(context, iniciarSesion(identificador, clave), Toast.LENGTH_LONG).show()
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

                            erroresCuenta = buildMap {
                                if (correo.isNotBlank() && almacen.correoDeIdentificador(correo) != null) put("correo", "Este correo ya está registrado.")
                                if (usuario.isNotBlank() && almacen.correoDeIdentificador(usuario) != null) put("usuario", "Este nombre de usuario ya existe.")
                            },
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
                                Toast.makeText(context, crearCuenta(), Toast.LENGTH_LONG).show()
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
                            nombreAdmin = perfil()?.optString("nombre") ?: "Administrador",
                            fotoAdmin = perfil()?.optString("foto"),
                            pendientes = SolicitudesLocal(context).todas().count { it.estado == "Pendiente" },
                            confirmadas = SolicitudesLocal(context).todas().count { it.estado == "Confirmado" },
                            stockBajo = almacen.productos().count { it.stock <= 2 },
                            onPerfil = { navegar("perfil") },
                            onNotificaciones = { navegar("notificaciones") },
                            cantidadProductos = almacen.productos().size,
                            productosDisponibles = almacen.productos().count { it.stock > 0 },
                            onGestionarProductos = {
                                pantallaActual = "adminProductos"
                            },
                            onCuentas = { pantallaActual = "adminCuentas" },
                            onCrearAdmin = { pantallaActual = "crearAdmin" },
                            onGestionarCampanas = {
                                pantallaActual = "adminCampanas"
                            },
                            onProximamente = { seccion ->
                                if (seccion == "Mapa de ferias") {
                                    pantallaActual = "mapaFerias"
                                } else if (seccion == "Gestión de solicitudes") {
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

                    "adminCuentas", "crearAdmin" -> {
                        if (esAdmin) PantallaCuentasAdmin(viewModel, pantallaActual == "crearAdmin", { pantallaActual = "admin" }, { pantallaActual = "crearAdmin" })
                        else LaunchedEffect(Unit) { pantallaActual = "login" }
                    }
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

                    "mapaFerias" -> {
                        PantallaMapaFerias(onVolver = { pantallaActual = if (perfil()?.optString("rol") == "ADMIN") "admin" else "inicio" }, esAdministrador = perfil()?.optString("rol") == "ADMIN", correoCliente = correoSesion, onIniciarSesion = { pantallaActual = "login" })
                    }

                    "misSolicitudes" -> {
                        PantallaSolicitudes(onVolver = { pantallaActual = "inicio" }, correoCliente = correoSesion)
                    }

                    "inicio", "usuario" -> {
                        PantallaInicio(viewModel, ::navegar,
                            correoSesion?.let { NotificacionesLocal(context).todas(it).count { n -> !n.leida } } ?: 0)
                    }
                    "perfil" -> {
                        if (correoSesion != null) PantallaPerfil(viewModel, ::volverInicio)
                        else LaunchedEffect(Unit) { pantallaActual = "login" }
                    }
                    "notificaciones" -> {
                        val cuenta = correoSesion
                        if (cuenta != null) PantallaNotificaciones(cuenta, ::volverInicio, avisoInicial = avisoDetalle)
                        else LaunchedEffect(Unit) { pantallaActual = "login" }
                    }
                    "catalogo", "favoritos" -> {
                        PantallaCatalogo(
                            almacen = almacen,
                            mostrarFavoritos = pantallaActual == "favoritos",
                            onNotificaciones = { navegar("notificaciones") },
                            onInicio = { pantallaActual = "inicio" },
                            correoCliente = correoSesion,
                            onMapa = { pantallaActual = "mapaFerias" },
                            onSolicitudes = {
                                pantallaActual = if (correoSesion != null) "misSolicitudes" else "login"
                            },
                            onPerfil = { navegar("perfil") }
                        )
                    }
                }
                    }
                    }
                    if (conMenu) MenuCliente(if (pantallaActual == "usuario") "inicio" else pantallaActual, ::navegar)
                }
    }
}
