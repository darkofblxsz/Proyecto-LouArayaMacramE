
package cl.louaraya.macrame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeLogin = Color(0xFF103C35)
private val CremaLogin = Color(0xFFF7E6CC)

@Composable
fun PantallaLogin(
    onIngresar: (String, String) -> Unit,
    onRegistrarse: () -> Unit,
    onVolver: () -> Unit
) {
    var usuarioCorreo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(VerdeLogin)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(65.dp))

            Text("✧", fontSize = 55.sp, color = CremaLogin)
            Text(
                "LOU ARAYA",
                fontSize = 29.sp,
                color = CremaLogin,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp
            )
            Text(
                "M A C R A M É",
                fontSize = 11.sp,
                color = CremaLogin
            )

            Spacer(modifier = Modifier.height(55.dp))

            Text(
                "Iniciar sesión",
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                "Usuario / Correo electrónico",
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = usuarioCorreo,
                onValueChange = { usuarioCorreo = it },
                placeholder = { Text("Usuario o correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = coloresLogin()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Contraseña",
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                placeholder = { Text("Contraseña") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (mostrarContrasena)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (mostrarContrasena)
                            "👁 Ocultar" else "👁 Ver",
                        color = CremaLogin,
                        modifier = Modifier.clickable {
                            mostrarContrasena = !mostrarContrasena
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = coloresLogin()
            )

            Spacer(modifier = Modifier.height(34.dp))

            Button(
                onClick = {
                    onIngresar(usuarioCorreo.trim(), contrasena)
                },
                enabled = usuarioCorreo.isNotBlank() &&
                        contrasena.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CremaLogin,
                    contentColor = VerdeLogin
                )
            ) {
                Text(
                    "Ingresar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "¿No tienes cuenta? ",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    "Regístrate",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        onRegistrarse()
                    }
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 8.dp)
                .size(48.dp)
                .clickable { onVolver() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", fontSize = 32.sp, color = CremaLogin)
        }
    }
}

@Composable
private fun coloresLogin() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = CremaLogin,
    unfocusedBorderColor = Color.Gray,
    focusedPlaceholderColor = Color.LightGray,
    unfocusedPlaceholderColor = Color.LightGray,
    cursorColor = CremaLogin
)
