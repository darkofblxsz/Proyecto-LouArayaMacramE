package cl.louaraya.macrame.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import cl.louaraya.macrame.R

private val VerdeOscuro = Color(0xFF123D35)

@Composable
fun PantallaBienvenida(
    onComenzar: () -> Unit,
    onIniciarSesion: () -> Unit
) {

    val entrada = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { entrada.animateTo(1f, tween(1000)) }
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
                modifier = Modifier.graphicsLayer { scaleX = entrada.value; scaleY = entrada.value; alpha = entrada.value },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(painterResource(R.drawable.la_mandala), "Logo Lou Araya", Modifier.size(110.dp))

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
