package cl.louaraya.macrame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.compose.runtime.*
import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.repository.AlmacenLocal
import cl.louaraya.macrame.repository.ImagenesCampanasRepository

/** Estado de presentación compartido. El repositorio conserva las claves de almacenamiento. */
class TiendaViewModel(application: Application, private val savedState: SavedStateHandle) : AndroidViewModel(application) {
    private val repository = AlmacenLocal(application)
    private val imagenesRepository = ImagenesCampanasRepository(application)
    private var pantallaActualState by mutableStateOf(savedState.get<String>("pantallaActual") ?: "bienvenida")
    var pantallaActual: String
        get() = pantallaActualState
        set(value) { pantallaActualState = value; savedState["pantallaActual"] = value }
    private var correoSesionState by mutableStateOf(savedState.get<String?>("correoSesion") ?: null)
    var correoSesion: String?
        get() = correoSesionState
        set(value) { correoSesionState = value; savedState["correoSesion"] = value }
    private var correoState by mutableStateOf(savedState.get<String>("correo") ?: "")
    var correo: String
        get() = correoState
        set(value) { correoState = value; savedState["correo"] = value }
    private var confirmarCorreoState by mutableStateOf(savedState.get<String>("confirmarCorreo") ?: "")
    var confirmarCorreo: String
        get() = confirmarCorreoState
        set(value) { confirmarCorreoState = value; savedState["confirmarCorreo"] = value }
    private var contrasenaState by mutableStateOf(savedState.get<String>("contrasena") ?: "")
    var contrasena: String
        get() = contrasenaState
        set(value) { contrasenaState = value; savedState["contrasena"] = value }
    private var confirmarContrasenaState by mutableStateOf(savedState.get<String>("confirmarContrasena") ?: "")
    var confirmarContrasena: String
        get() = confirmarContrasenaState
        set(value) { confirmarContrasenaState = value; savedState["confirmarContrasena"] = value }
    private var nombreState by mutableStateOf(savedState.get<String>("nombre") ?: "")
    var nombre: String
        get() = nombreState
        set(value) { nombreState = value; savedState["nombre"] = value }
    private var apellidoState by mutableStateOf(savedState.get<String>("apellido") ?: "")
    var apellido: String
        get() = apellidoState
        set(value) { apellidoState = value; savedState["apellido"] = value }
    private var fechaNacimientoState by mutableStateOf(savedState.get<String>("fechaNacimiento") ?: "")
    var fechaNacimiento: String
        get() = fechaNacimientoState
        set(value) { fechaNacimientoState = value; savedState["fechaNacimiento"] = value }
    private var usuarioState by mutableStateOf(savedState.get<String>("usuario") ?: "")
    var usuario: String
        get() = usuarioState
        set(value) { usuarioState = value; savedState["usuario"] = value }
    private var codigoPaisState by mutableStateOf(savedState.get<String>("codigoPais") ?: "+56")
    var codigoPais: String
        get() = codigoPaisState
        set(value) { codigoPaisState = value; savedState["codigoPais"] = value }
    private var telefonoState by mutableStateOf(savedState.get<String>("telefono") ?: "")
    var telefono: String
        get() = telefonoState
        set(value) { telefonoState = value; savedState["telefono"] = value }
    private var aceptaTerminosState by mutableStateOf(savedState.get<Boolean>("aceptaTerminos") ?: false)
    var aceptaTerminos: Boolean
        get() = aceptaTerminosState
        set(value) { aceptaTerminosState = value; savedState["aceptaTerminos"] = value }

    var avisoPendiente by mutableStateOf<String?>(null)
    var correoAvisoPendiente by mutableStateOf<String?>(null)
    var avisoDetalle by mutableStateOf<String?>(null)
    var categoriaInicio by mutableStateOf("Todas")

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
    fun iniciarSesion(identificador: String, clave: String): String {
        val rol = repository.autenticar(identificador, clave)
        if (rol != "ADMIN" && rol != "USER") return "Correo, usuario o contraseña incorrectos"
        correoSesion = repository.correoDeIdentificador(identificador)
        pantallaActual = if (rol == "ADMIN") "admin" else "inicio"
        return if (rol == "ADMIN") "Bienvenido administrador" else "Inicio de sesión correcto"
    }

    fun cerrarSesion() {
        correoSesion = null
        pantallaActual = "login"
    }

    fun crearCuenta(): String {
        val errores = erroresRegistro(correo, confirmarCorreo, contrasena, confirmarContrasena,
            nombre, apellido, fechaNacimiento, usuario, codigoPais, telefono, aceptaTerminos)
        if (errores.isNotEmpty()) return errores.values.first()
        if (correo.trim().isEmpty()) return "Ingresa tu correo"
        if (!correo.trim().equals(confirmarCorreo.trim(), ignoreCase = true)) return "Los correos no coinciden"
        if (contrasena != confirmarContrasena) return "Las contraseñas no coinciden"
        if (!aceptaTerminos) return "Debes aceptar los términos y condiciones"
        val error = repository.registrar(correo, contrasena, nombre, usuario, apellido,
            fechaNacimiento, codigoPais + telefono)
        if (error != null) return error
        limpiarRegistro()
        pantallaActual = "login"
        return "Cuenta creada correctamente. Ahora inicia sesión."
    }

    private val favoritosPrefs = application.getSharedPreferences("favoritos", 0)
    fun favoritos(): Set<Int> = favoritosPrefs.getStringSet(correoSesion ?: "visitante", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()
    fun guardarFavoritos(ids: Set<Int>) { favoritosPrefs.edit().putStringSet(correoSesion ?: "visitante", ids.map { it.toString() }.toSet()).apply() }
    fun cuentasAdmin() = repository.cuentasAdmin(correoSesion ?: "")
    fun guardarCuentaAdmin(original: String?, correo: String, usuario: String, nombre: String, apellido: String, telefono: String, clave: String) =
        repository.guardarCuentaAdmin(correoSesion ?: "", original, correo, usuario, nombre, apellido, telefono, clave)
    fun eliminarCuentaAdmin(correo: String) = repository.eliminarCuentaAdmin(correoSesion ?: "", correo)
    fun perfil() = correoSesion?.let { repository.perfil(it) }
    fun actualizarPerfil(nombre: String, apellido: String, telefono: String, foto: String, actual: String, nueva: String) =
        correoSesion?.let { repository.actualizarPerfil(it, nombre, apellido, telefono, foto, actual, nueva) } ?: "Inicia sesión."

    fun productos() = repository.productos()
    fun guardarProducto(producto: ProductoLocal) = repository.guardarProducto(producto)
    fun eliminarProducto(id: Int) = repository.eliminarProducto(id)
    fun siguienteIdProducto() = repository.siguienteIdProducto()
    fun campanas() = repository.campanas()
    fun guardarCampanas(campanas: List<Campana>) = repository.guardarCampanas(campanas)
    fun autenticar(identificador: String, password: String) = repository.autenticar(identificador, password)
    fun correoDeIdentificador(identificador: String) = repository.correoDeIdentificador(identificador)
    fun registrar(email: String, password: String, nombre: String, usuario: String, apellido: String,
                  fechaNacimiento: String, telefono: String) =
        repository.registrar(email, password, nombre, usuario, apellido, fechaNacimiento, telefono)
    fun imagenesCampanas() = imagenesRepository.leer()
    fun guardarImagenesCampanas(json: String) = imagenesRepository.guardar(json)
}
