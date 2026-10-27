package cl.louaraya.macrame.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

fun erroresRegistro(correo: String, confirmacion: String, clave: String, confirmarClave: String,
    nombre: String, apellido: String, fecha: String, usuario: String, codigo: String,
    telefono: String, terminos: Boolean): Map<String, String> = buildMap {
    if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(correo.trim())) put("correo", "Ingresa un correo válido, por ejemplo nombre@gmail.com.")
    else if (correo.trim().endsWith("@admin.com", true)) put("correo", "Este dominio está reservado para administración.")
    if (confirmacion.isBlank() || !correo.trim().equals(confirmacion.trim(), true)) put("confirmarCorreo", "Repite el mismo correo electrónico.")
    if (clave.length < 8) put("clave", "La contraseña debe tener al menos 8 caracteres.")
    if (confirmarClave.isEmpty() || clave != confirmarClave) put("confirmarClave", "Las contraseñas deben ser iguales.")
    if (nombre.isBlank()) put("nombre", "Escribe tu nombre.")
    if (apellido.isBlank()) put("apellido", "Escribe tu apellido.")
    val nacimiento = runCatching { LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)) }.getOrNull()
    if (nacimiento == null || nacimiento.isAfter(LocalDate.now())) put("fecha", "Selecciona una fecha de nacimiento válida, no futura.")
    if (usuario.isBlank()) put("usuario", "Elige un nombre de usuario.")
    if (!Regex("\\+[0-9]{1,4}").matches(codigo)) put("codigo", "Usa + y el código del país, por ejemplo +56.")
    if (!Regex("[0-9]{7,15}").matches(telefono)) put("telefono", "Escribe entre 7 y 15 dígitos, sin letras ni espacios.")
    if (!terminos) put("terminos", "Debes aceptar los términos para crear tu cuenta.")
}
