
package cl.louaraya.macrame

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class Campana(
    val nombre: String,
    val descripcion: String,
    val activa: Boolean
)

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

    // Mantiene las cuentas de prueba disponibles aunque ya existan
    // usuarios guardados de versiones anteriores de la aplicación.
    // No elimina cuentas registradas, productos ni campañas.
    private fun crearCuentasPrueba() {
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
            val correcta = if (cuentaActual == null) {
                false
            } else {
                try {
                    val salt = Base64.decode(
                        cuentaActual.getString("salt"), Base64.NO_WRAP
                    )
                    val hash = generarHash(clave, salt)
                    cuentaActual.optString("rol") == prueba[4] &&
                        MessageDigest.isEqual(
                            hash.toByteArray(),
                            cuentaActual.getString("hash").toByteArray()
                        )
                } catch (e: Exception) {
                    false
                }
            }

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
            prefs.edit().putString("cuentas", cuentas.toString()).apply()
        }
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
