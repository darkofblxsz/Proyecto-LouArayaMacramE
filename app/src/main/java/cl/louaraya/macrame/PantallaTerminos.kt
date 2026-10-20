
package cl.louaraya.macrame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeTerminos = Color(0xFF103C35)
private val CremaTerminos = Color(0xFFF7E6CC)

@Composable
fun PantallaTerminos(
    onVolver: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdeTerminos)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 26.dp,
                    end = 26.dp,
                    top = 78.dp,
                    bottom = 40.dp
                )
        ) {

            Text(
                "Términos y condiciones",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = CremaTerminos
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "LOU ARAYA MACRAMÉ",
                fontSize = 14.sp,
                color = CremaTerminos,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(25.dp))

            SeccionTerminos(
                "1. Introducción",
                "Bienvenido a Lou Araya Macramé. " +
                        "Esta aplicación está diseñada para facilitar " +
                        "el acceso a información sobre productos " +
                        "artesanales y sus servicios asociados."
            )

            SeccionTerminos(
                "2. Registro de usuarios",
                "Para crear una cuenta, debes proporcionar " +
                        "información correcta y mantenerla actualizada. " +
                        "Eres responsable de proteger tus credenciales " +
                        "y de no compartir tu contraseña con terceros."
            )

            SeccionTerminos(
                "3. Uso de la aplicación",
                "La aplicación debe utilizarse de forma " +
                        "respetuosa y conforme a la legislación aplicable. " +
                        "No está permitido intentar acceder sin " +
                        "autorización a cuentas o sistemas ajenos."
            )

            SeccionTerminos(
                "4. Información personal",
                "Durante el registro se solicitan datos como " +
                        "nombre, apellido, correo electrónico, fecha " +
                        "de nacimiento y teléfono. Antes de publicar " +
                        "la aplicación, se deberá informar claramente " +
                        "para qué se utilizarán estos datos, cómo se " +
                        "protegerán y cómo ejercer los derechos " +
                        "relacionados con su tratamiento."
            )

            SeccionTerminos(
                "5. Contraseñas y seguridad",
                "Las contraseñas deberán protegerse mediante " +
                        "mecanismos de seguridad apropiados. " +
                        "No deben almacenarse como texto sin protección."
            )

            SeccionTerminos(
                "6. Productos y servicios",
                "Las características, precios, disponibilidad " +
                        "y condiciones de compra de los productos " +
                        "deberán informarse claramente antes de " +
                        "realizar cualquier transacción."
            )

            SeccionTerminos(
                "7. Actualizaciones",
                "Las condiciones podrán actualizarse " +
                        "cuando sea necesario. Los cambios relevantes " +
                        "deberán comunicarse a los usuarios de " +
                        "manera apropiada."
            )

            SeccionTerminos(
                "8. Contacto",
                "Antes de publicar la aplicación se deberá " +
                        "incorporar un canal oficial de contacto " +
                        "para consultas sobre el servicio y " +
                        "tratamiento de datos personales."
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                "Al regresar al registro podrás decidir " +
                        "si aceptas o no estas condiciones.",
                color = CremaTerminos,
                fontSize = 14.sp
            )
        }

        // FLECHA PARA VOLVER AL REGISTRO
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
                color = CremaTerminos
            )
        }
    }
}

@Composable
private fun SeccionTerminos(
    titulo: String,
    contenido: String
) {
    Text(
        titulo,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = CremaTerminos
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        contenido,
        fontSize = 14.sp,
        lineHeight = 23.sp,
        color = Color.White
    )

    Spacer(modifier = Modifier.height(25.dp))
}
