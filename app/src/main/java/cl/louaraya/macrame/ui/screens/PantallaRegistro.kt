
package cl.louaraya.macrame.ui.screens

import cl.louaraya.macrame.R
import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.viewmodel.*
import cl.louaraya.macrame.ui.components.*

import android.app.DatePickerDialog
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import androidx.compose.runtime.saveable.rememberSaveable
private val VerdeRegistro = Color(0xFF103C35)
private val CremaRegistro = Color(0xFFF7E6CC)
private val AzulEnlace = Color(0xFF8FCBFF)

@Composable
fun PantallaRegistro(
    correo: String,
    onCorreoChange: (String) -> Unit,
    confirmarCorreo: String,
    onConfirmarCorreoChange: (String) -> Unit,
    contrasena: String,
    onContrasenaChange: (String) -> Unit,
    confirmarContrasena: String,
    onConfirmarContrasenaChange: (String) -> Unit,
    nombre: String,
    onNombreChange: (String) -> Unit,
    apellido: String,
    onApellidoChange: (String) -> Unit,
    fechaNacimiento: String,
    onFechaNacimientoChange: (String) -> Unit,
    usuario: String,
    onUsuarioChange: (String) -> Unit,
    codigoPais: String,
    onCodigoPaisChange: (String) -> Unit,
    telefono: String,
    onTelefonoChange: (String) -> Unit,
    aceptaTerminos: Boolean,
    onAceptaTerminosChange: (Boolean) -> Unit,
    onVolver: () -> Unit,
    onLeerTerminos: () -> Unit,
    erroresCuenta: Map<String, String> = emptyMap(),
    onCrearCuenta: () -> Unit
) {
    var mostrarContrasena by rememberSaveable {
        mutableStateOf(false)
    }
    var mostrarConfirmacion by rememberSaveable {
        mutableStateOf(false)
    }

    var intento by rememberSaveable { mutableStateOf(false) }
    val errores = erroresRegistro(correo, confirmarCorreo, contrasena, confirmarContrasena,
        nombre, apellido, fechaNacimiento, usuario, codigoPais, telefono, aceptaTerminos) + erroresCuenta
    fun error(campo: String, valor: String) = if (intento || valor.isNotEmpty()) errores[campo] else null
    val context = LocalContext.current
    val hoy = Calendar.getInstance()

    val fechaDialogo = DatePickerDialog(
        context,
        { _, anio, mes, dia ->
            onFechaNacimientoChange(
                "%02d/%02d/%04d".format(
                    dia, mes + 1, anio
                )
            )
        },
        hoy.get(Calendar.YEAR) - 18,
        hoy.get(Calendar.MONTH),
        hoy.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdeRegistro)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(75.dp))

            Text(
                "✧",
                fontSize = 45.sp,
                color = CremaRegistro
            )

            Text(
                "LOU ARAYA",
                fontSize = 27.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp,
                color = CremaRegistro
            )

            Text(
                "M A C R A M É",
                fontSize = 11.sp,
                color = CremaRegistro
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                "Crear cuenta",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Completa tus datos para registrarte",
                color = Color.LightGray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(26.dp))

            // ========================================
            // CORREOS ELECTRÓNICOS
            // ========================================

            CampoRegistro(
                titulo = "Correo electrónico",
                valor = correo,
                error = error("correo", correo),
                alCambiar = onCorreoChange,
                ejemplo = "ejemplo@correo.com",
                tipo = KeyboardType.Email
            )

            CampoRegistro(
                titulo = "Confirmar correo electrónico",
                valor = confirmarCorreo,
                error = error("confirmarCorreo", confirmarCorreo),
                alCambiar = onConfirmarCorreoChange,
                ejemplo = "Repite tu correo",
                tipo = KeyboardType.Email
            )


            // ========================================
            // CONTRASEÑAS
            // ========================================

            CampoRegistro(
                titulo = "Contraseña",
                valor = contrasena,
                error = error("clave", contrasena),
                alCambiar = onContrasenaChange,
                ejemplo = "Mínimo 8 caracteres",
                esContrasena = true,
                mostrar = mostrarContrasena,
                alCambiarVisibilidad = {
                    mostrarContrasena = !mostrarContrasena
                }
            )

            CampoRegistro(
                titulo = "Confirmar contraseña",
                valor = confirmarContrasena,
                error = error("confirmarClave", confirmarContrasena),
                alCambiar = onConfirmarContrasenaChange,
                ejemplo = "Repite tu contraseña",
                esContrasena = true,
                mostrar = mostrarConfirmacion,
                alCambiarVisibilidad = {
                    mostrarConfirmacion = !mostrarConfirmacion
                }
            )


            // ========================================
            // DATOS PERSONALES
            // ========================================

            CampoRegistro(
                titulo = "Nombre",
                valor = nombre,
                error = error("nombre", nombre),
                alCambiar = onNombreChange,
                ejemplo = "Tu nombre"
            )

            CampoRegistro(
                titulo = "Apellido",
                valor = apellido,
                error = error("apellido", apellido),
                alCambiar = onApellidoChange,
                ejemplo = "Tu apellido"
            )

            Text(
                "Fecha de nacimiento",
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { fechaDialogo.show() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CremaRegistro
                )
            ) {
                Text(
                    fechaNacimiento.ifEmpty { "Seleccionar fecha" }
                )
            }

            if (intento) errores["fecha"]?.let { MensajeErrorRegistro(it) }
            Spacer(modifier = Modifier.height(18.dp))

            CampoRegistro(
                titulo = "Nombre de usuario",
                valor = usuario,
                error = error("usuario", usuario),
                alCambiar = onUsuarioChange,
                ejemplo = "Crea tu usuario"
            )

            // ========================================
            // TELÉFONO
            // ========================================

            Text(
                "Número de teléfono",
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = codigoPais,
                    onValueChange = onCodigoPaisChange,
                    label = { Text("Código") },
                    isError = error("codigo", codigoPais) != null,
                    singleLine = true,
                    modifier = Modifier.width(110.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    colors = coloresRegistro()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = onTelefonoChange,
                    isError = error("telefono", telefono) != null,
                    placeholder = { Text("Número") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    colors = coloresRegistro()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Chile: +56. Para otro país, cambia el código.",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.fillMaxWidth()
            )

            error("codigo", codigoPais)?.let { MensajeErrorRegistro(it) }
            error("telefono", telefono)?.let { MensajeErrorRegistro(it) }
            Spacer(modifier = Modifier.height(28.dp))

            // ========================================
            // TÉRMINOS Y CONDICIONES
            // ========================================

            HorizontalDivider(
                color = CremaRegistro.copy(alpha = 0.35f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Términos y condiciones",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = CremaRegistro,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aceptaTerminos,
                    onCheckedChange = onAceptaTerminosChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = CremaRegistro,
                        uncheckedColor = CremaRegistro,
                        checkmarkColor = VerdeRegistro
                    )
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Acepto los términos y condiciones",
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Text(
                        "Leer más",
                        fontSize = 14.sp,
                        color = AzulEnlace,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { onLeerTerminos() }
                            .padding(vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========================================
            // CREAR CUENTA
            // ========================================

            if (intento) errores["terminos"]?.let { MensajeErrorRegistro(it) }
            Button(
                onClick = { intento = true; if (errores.isEmpty()) onCrearCuenta() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CremaRegistro,
                    contentColor = VerdeRegistro
                )
            ) {
                Text(
                    "Crear cuenta",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Tus datos se guardarán en este dispositivo.",
                fontSize = 12.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // ========================================
        // FLECHA DE REGRESO
        // ========================================

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 8.dp)
                .size(48.dp)
                .clickable { onVolver() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "←",
                fontSize = 32.sp,
                color = CremaRegistro
            )
        }
    }
}

// ========================================
// COMPONENTE REUTILIZABLE DE CAMPOS
// ========================================

@Composable
private fun CampoRegistro(
    titulo: String,
    valor: String,
    alCambiar: (String) -> Unit,
    ejemplo: String,
    error: String? = null,
    tipo: KeyboardType = KeyboardType.Text,
    esContrasena: Boolean = false,
    mostrar: Boolean = false,
    alCambiarVisibilidad: () -> Unit = {}
) {
    Text(
        titulo,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = valor,
        isError = error != null,
        supportingText = { if (error != null) Text(error, color = Color(0xFFFF8A80)) },
        onValueChange = alCambiar,
        placeholder = { Text(ejemplo) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esContrasena)
                KeyboardType.Password
            else tipo
        ),
        visualTransformation =
            if (esContrasena && !mostrar)
                PasswordVisualTransformation()
            else VisualTransformation.None,
        trailingIcon = if (esContrasena) {
            {
                Text(
                    text = if (mostrar)
                        "👁 Ocultar"
                    else "👁 Ver",
                    color = CremaRegistro,
                    modifier = Modifier.clickable {
                        alCambiarVisibilidad()
                    }
                )
            }
        } else null,
        colors = coloresRegistro()
    )

    Spacer(modifier = Modifier.height(18.dp))
}

@Composable
private fun MensajeErrorRegistro(mensaje: String) {
    Text(
        mensaje,
        color = Color(0xFFFFAAAA),
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
private fun coloresRegistro() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = CremaRegistro,
        unfocusedBorderColor = Color.Gray,
        focusedPlaceholderColor = Color.LightGray,
        unfocusedPlaceholderColor = Color.LightGray,
        focusedLabelColor = CremaRegistro,
        unfocusedLabelColor = Color.LightGray,
        cursorColor = CremaRegistro
    )
