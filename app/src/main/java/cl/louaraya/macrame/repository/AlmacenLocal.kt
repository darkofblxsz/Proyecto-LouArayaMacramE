
package cl.louaraya.macrame.repository

import cl.louaraya.macrame.model.*

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec





class AlmacenLocal(context: Context) {

    private val prefs = context.getSharedPreferences(
        "lou_araya_local_v1",
        Context.MODE_PRIVATE
    )

    private val random = SecureRandom()

    init {
        crearCuentasPrueba()
        crearCampanasIniciales()
        crearProductosIniciales()
    }

    // Crea las cuentas de demostración solo si faltan. Nunca restablece una
    // contraseña o foto que el usuario haya cambiado.
    private fun crearCuentasPrueba() {
        if (prefs.getBoolean("cuentas_demo_inicializadas", false)) return
        val cuentas = try {
            JSONArray(prefs.getString("cuentas", "[]") ?: "[]")
        } catch (e: Exception) {
            JSONArray()
        }

        val pruebas = listOf(
            listOf("admin@admin.com", "Admin1234!", "Administrador", "Admin", "ADMIN"),
            listOf("usuario@gmail.com", "Usuario1234!", "Usuario", "Usuario", "USER")
        )

        var cambio = false
        for (prueba in pruebas) {
            val correo = prueba[0]
            val clave = prueba[1]
            val indice = (0 until cuentas.length()).firstOrNull { i ->
                cuentas.optJSONObject(i)?.optString("correo")
                    ?.equals(correo, ignoreCase = true) == true
            }

            val cuentaActual = indice?.let { cuentas.optJSONObject(it) }
            val correcta = cuentaActual != null

            if (!correcta) {
                val cuentaNueva = crearCuenta(
                    correo, clave, prueba[2], prueba[3], prueba[4]
                )
                if (indice == null) cuentas.put(cuentaNueva)
                else cuentas.put(indice, cuentaNueva)
                cambio = true
            }
        }

        if (cambio || !prefs.contains("cuentas")) {
            prefs.edit().putString("cuentas", cuentas.toString()).putBoolean("cuentas_demo_inicializadas", true).apply()
        }
    }

    private fun cuentasJson() = JSONArray(prefs.getString("cuentas", "[]"))
    private fun esAdmin(actor: String) = perfil(actor)?.optString("rol") == "ADMIN"

    fun cuentasAdmin(actor: String): List<CuentaResumen> {
        if (!esAdmin(actor)) return emptyList()
        val a = cuentasJson()
        return (0 until a.length()).map { a.getJSONObject(it) }.map {
            CuentaResumen(it.getString("correo"), it.getString("usuario"), it.optString("nombre"), it.optString("apellido"), it.optString("telefono"), it.getString("rol"))
        }.sortedBy { it.nombre.lowercase() }
    }

    fun guardarCuentaAdmin(actor: String, original: String?, correo: String, usuario: String,
        nombre: String, apellido: String, telefono: String, clave: String): String? {
        if (!esAdmin(actor)) return "Solo un administrador puede gestionar cuentas."
        val email = correo.trim().lowercase()
        if (original == null && email in (prefs.getStringSet("correos_eliminados", emptySet()) ?: emptySet())) return "Este correo pertenece a una cuenta eliminada; usa otro correo."
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Ingresa un correo válido."
        if (nombre.isBlank() || usuario.isBlank()) return "Nombre y usuario son obligatorios."
        if (telefono.isNotBlank() && !Regex("\\+?[0-9]{7,15}").matches(telefono)) return "El teléfono debe contener entre 7 y 15 dígitos."
        if ((original == null || clave.isNotEmpty()) && clave.length < 8) return "La contraseña debe tener al menos 8 caracteres."
        if (original != null && !email.equals(original, true)) return "El correo identifica los pedidos y no se puede modificar."
        val a = cuentasJson()
        val indice = (0 until a.length()).firstOrNull { a.getJSONObject(it).getString("correo").equals(original, true) }
        if (original != null && indice == null) return "Esta cuenta ya no existe."
        for (i in 0 until a.length()) {
            if (i == indice) continue
            val c = a.getJSONObject(i)
            if (c.getString("correo").equals(email, true)) return "Este correo ya está registrado."
            if (c.getString("usuario").equals(usuario.trim(), true)) return "Este usuario ya está registrado."
        }
        val c = if (indice == null) crearCuenta(email, clave, nombre.trim(), usuario.trim(), "ADMIN") else a.getJSONObject(indice)
        c.put("nombre", nombre.trim()).put("apellido", apellido.trim()).put("usuario", usuario.trim()).put("telefono", telefono.trim())
        if (indice != null && clave.isNotEmpty()) {
            val salt = ByteArray(16).also { random.nextBytes(it) }
            c.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP)).put("hash", generarHash(clave, salt))
        }
        if (indice == null) a.put(c) else a.put(indice, c)
        return if (prefs.edit().putString("cuentas", a.toString()).commit()) null else "No se pudo guardar la cuenta."
    }

    fun eliminarCuentaAdmin(actor: String, correo: String): String? {
        if (!esAdmin(actor)) return "Solo un administrador puede eliminar cuentas."
        if (actor.equals(correo, true)) return "No puedes eliminar la cuenta con la que estás trabajando."
        val a = cuentasJson()
        val lista = (0 until a.length()).map { a.getJSONObject(it) }
        val actual = lista.firstOrNull { it.getString("correo").equals(correo, true) } ?: return "La cuenta ya no existe."
        if (actual.getString("rol") == "ADMIN" && lista.count { it.getString("rol") == "ADMIN" } <= 1) return "Debe quedar al menos un administrador."
        val solicitudes = JSONArray(prefs.getString("solicitudes_reales_v1", "[]"))
        for (i in 0 until solicitudes.length()) {
            val pedido = solicitudes.getJSONObject(i)
            if (pedido.optString("correo").equals(correo, true) && pedido.optString("estado") in listOf("Pendiente", "Confirmado"))
                return "Esta cuenta tiene pedidos activos. Resuélvelos o cancélalos antes de eliminarla."
        }
        val nuevas = JSONArray()
        lista.filterNot { it.getString("correo").equals(correo, true) }.forEach { nuevas.put(it) }
        val eliminados = (prefs.getStringSet("correos_eliminados", emptySet()) ?: emptySet()) + correo.lowercase()
        return if (prefs.edit().putStringSet("correos_eliminados", eliminados).putString("cuentas", nuevas.toString()).putBoolean("cuentas_demo_inicializadas", true).commit()) null else "No se pudo eliminar la cuenta."
    }

    private fun crearCampanasIniciales() {
        if (prefs.contains("campanas")) return

        val campanas = listOf(
            Campana(
                "Matrimonios",
                "Colección especial",
                true
            ),
            Campana(
                "Festivales",
                "Decoración única",
                true
            ),
            Campana(
                "Hogar",
                "Estilo y calidez",
                true
            ),
            Campana(
                "Viajes",
                "Lleva el arte contigo",
                false
            )
        )

        guardarCampanas(campanas)
    }

    private fun crearProductosIniciales() {
        if (prefs.contains("productos")) return

        val iniciales = listOf(
            ProductoLocal(1, "MAC-001", "Tapiz Boho", "Hogar", "Algodón natural", "80 × 120 cm", 45000, 5, "hogar"),
            ProductoLocal(2, "MAC-002", "Decoración Floral", "Matrimonios", "Algodón natural", "120 × 180 cm", 62000, 3, "matrimonios"),
            ProductoLocal(3, "MAC-003", "Atrapasueños", "Festivales", "Algodón y madera", "45 × 70 cm", 28000, 8, "festivales"),
            ProductoLocal(4, "MAC-004", "Cortina Macramé", "Hogar", "Algodón", "90 × 180 cm", 55000, 4, "hogar"),
            ProductoLocal(5, "MAC-005", "Fondo Ceremonial", "Matrimonios", "Algodón crudo", "140 × 200 cm", 85000, 2, "matrimonios"),
            ProductoLocal(6, "MAC-006", "Colgante Festival", "Festivales", "Algodón natural", "60 × 90 cm", 38000, 6, "festivales"),
            ProductoLocal(7, "MAC-007", "Mini Tapiz Viajero", "Viajes", "Algodón", "25 × 40 cm", 18000, 10, "viajes"),
            ProductoLocal(8, "MAC-008", "Colgante Artesanal", "Viajes", "Yute y madera", "35 × 60 cm", 24000, 0, "viajes")
        )
        guardarProductos(iniciales)
    }

    fun perfil(correo: String): JSONObject? {
        val cuentas = JSONArray(prefs.getString("cuentas", "[]"))
        return (0 until cuentas.length()).map { cuentas.getJSONObject(it) }
            .firstOrNull { it.optString("correo").equals(correo, true) }
    }

    fun actualizarPerfil(correo: String, nombre: String, apellido: String, telefono: String,
                         foto: String, claveActual: String, nuevaClave: String): String? {
        if (nombre.isBlank()) return "Escribe tu nombre."
        if (telefono.isNotBlank() && !Regex("\\+?[0-9]{7,15}").matches(telefono)) return "Revisa el teléfono (7 a 15 dígitos)."
        if (nuevaClave.isNotEmpty() && nuevaClave.length < 8) return "La nueva contraseña requiere 8 caracteres."
        if (nuevaClave.isNotEmpty() && autenticar(correo, claveActual) == null) return "La contraseña actual es incorrecta."
        val cuentas = JSONArray(prefs.getString("cuentas", "[]"))
        val indice = (0 until cuentas.length()).firstOrNull { cuentas.getJSONObject(it).optString("correo").equals(correo, true) }
            ?: return "La cuenta no existe."
        val cuenta = cuentas.getJSONObject(indice)
        cuenta.put("nombre", nombre.trim()).put("apellido", apellido.trim()).put("telefono", telefono).put("foto", foto)
        if (nuevaClave.isNotEmpty()) {
            val salt = ByteArray(16).also { random.nextBytes(it) }
            cuenta.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP)).put("hash", generarHash(nuevaClave, salt))
        }
        return if (prefs.edit().putString("cuentas", cuentas.toString()).commit()) null else "No se pudieron guardar los datos."
    }

    fun productos(): List<ProductoLocal> {
        val array = JSONArray(prefs.getString("productos", "[]"))
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            ProductoLocal(
                id = item.getInt("id"),
                codigo = item.getString("codigo"),
                nombre = item.getString("nombre"),
                categoria = item.getString("categoria"),
                material = item.getString("material"),
                dimensiones = item.getString("dimensiones"),
                precio = item.getInt("precio"),
                stock = item.getInt("stock"),
                imagenKey = item.optString("imagenKey", "hogar"),
                fotos = item.optJSONArray("fotos")?.let { array ->
                    (0 until array.length()).map { array.getString(it) }
                } ?: emptyList()
            )
        }
    }

    fun guardarProducto(producto: ProductoLocal) {
        val lista = productos().toMutableList()
        val indice = lista.indexOfFirst { it.id == producto.id }
        if (indice >= 0) lista[indice] = producto else lista.add(producto)
        guardarProductos(lista)
    }

    fun eliminarProducto(id: Int) {
        guardarProductos(productos().filterNot { it.id == id })
    }

    fun siguienteIdProducto(): Int {
        // No reutilizar códigos aunque se elimine el último producto.
        val id = maxOf(prefs.getInt("ultimo_id_producto", 0), productos().maxOfOrNull { it.id } ?: 0) + 1
        prefs.edit().putInt("ultimo_id_producto", id).apply()
        return id
    }

    fun correoDeIdentificador(identificador: String): String? {
        val cuentas = JSONArray(prefs.getString("cuentas", "[]"))
        return (0 until cuentas.length()).map { cuentas.getJSONObject(it) }.firstOrNull {
            it.getString("correo").equals(identificador.trim(), true) ||
                it.getString("usuario").equals(identificador.trim(), true)
        }?.getString("correo")
    }

    private fun guardarProductos(productos: List<ProductoLocal>) {
        val array = JSONArray()
        productos.forEach { producto ->
            array.put(
                JSONObject()
                    .put("id", producto.id)
                    .put("codigo", producto.codigo)
                    .put("nombre", producto.nombre)
                    .put("categoria", producto.categoria)
                    .put("material", producto.material)
                    .put("dimensiones", producto.dimensiones)
                    .put("precio", producto.precio)
                    .put("stock", producto.stock)
                    .put("imagenKey", producto.imagenKey)
                    .put("fotos", JSONArray(producto.fotos))
            )
        }
        prefs.edit().putString("productos", array.toString()).apply()
    }

    private fun generarHash(
        password: String,
        salt: ByteArray
    ): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            120000,
            256
        )

        return try {
            val bytes = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded

            Base64.encodeToString(
                bytes,
                Base64.NO_WRAP
            )
        } finally {
            spec.clearPassword()
        }
    }

    private fun crearCuenta(
        email: String,
        password: String,
        nombre: String,
        usuario: String,
        rol: String
    ): JSONObject {

        val salt = ByteArray(16)
        random.nextBytes(salt)

        return JSONObject()
            .put("correo", email.trim().lowercase())
            .put("nombre", nombre)
            .put("usuario", usuario)
            .put("rol", rol)
            .put(
                "salt",
                Base64.encodeToString(
                    salt,
                    Base64.NO_WRAP
                )
            )
            .put("hash", generarHash(password, salt))
    }

    fun registrar(
        email: String,
        password: String,
        nombre: String,
        usuario: String,
        apellido: String,
        fechaNacimiento: String,
        telefono: String
    ): String? {

        val correo = email.trim().lowercase()
        val nombreUsuario = usuario.trim()
        if (correo in (prefs.getStringSet("correos_eliminados", emptySet()) ?: emptySet())) return "Este correo pertenece a una cuenta eliminada; usa otro correo."

        if (correo.isBlank() ||
            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(correo).matches()
        ) {
            return "Ingresa un correo válido"
        }

        if (correo.endsWith("@admin.com")) {
            return "El dominio @admin.com está reservado"
        }

        if (password.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres"
        }

        if (nombre.isBlank() ||
            apellido.isBlank() ||
            nombreUsuario.isBlank()
        ) {
            return "Completa todos los datos obligatorios"
        }

        val cuentas = JSONArray(
            prefs.getString("cuentas", "[]")
        )

        for (i in 0 until cuentas.length()) {
            val cuenta = cuentas.getJSONObject(i)

            if (cuenta.getString("correo") == correo) {
                return "Este correo ya está registrado"
            }

            if (cuenta.getString("usuario")
                    .equals(nombreUsuario, ignoreCase = true)
            ) {
                return "Este nombre de usuario ya existe"
            }
        }

        val nuevaCuenta = crearCuenta(
            correo,
            password,
            nombre.trim(),
            nombreUsuario,
            "USER"
        )

        nuevaCuenta
            .put("apellido", apellido.trim())
            .put("fechaNacimiento", fechaNacimiento)
            .put("telefono", telefono)

        cuentas.put(nuevaCuenta)

        prefs.edit()
            .putString("cuentas", cuentas.toString())
            .apply()

        return null
    }

    fun autenticar(
        identificador: String,
        password: String
    ): String? {

        val cuentas = JSONArray(
            prefs.getString("cuentas", "[]")
        )

        for (i in 0 until cuentas.length()) {
            val cuenta = cuentas.getJSONObject(i)

            val coincideCorreo = cuenta
                .getString("correo")
                .equals(
                    identificador.trim(),
                    ignoreCase = true
                )

            val coincideUsuario = cuenta
                .getString("usuario")
                .equals(
                    identificador.trim(),
                    ignoreCase = true
                )

            if (coincideCorreo || coincideUsuario) {

                val salt = Base64.decode(
                    cuenta.getString("salt"),
                    Base64.NO_WRAP
                )

                val hashIngresado = generarHash(
                    password,
                    salt
                )

                val hashGuardado = cuenta.getString("hash")

                if (
                    MessageDigest.isEqual(
                        hashIngresado.toByteArray(),
                        hashGuardado.toByteArray()
                    )
                ) {
                    return cuenta.getString("rol")
                }
            }
        }

        return null
    }

    fun campanas(): List<Campana> {

        val array = JSONArray(
            prefs.getString("campanas", "[]")
        )

        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)

            Campana(
                nombre = item.getString("nombre"),
                descripcion = item.getString("descripcion"),
                activa = item.getBoolean("activa")
            )
        }
    }

    fun guardarCampanas(
        campanas: List<Campana>
    ) {
        val array = JSONArray()

        campanas.forEach { campana ->
            array.put(
                JSONObject()
                    .put("nombre", campana.nombre)
                    .put("descripcion", campana.descripcion)
                    .put("activa", campana.activa)
            )
        }

        prefs.edit()
            .putString("campanas", array.toString())
            .apply()
    }
}
