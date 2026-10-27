package cl.louaraya.macrame

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import cl.louaraya.macrame.ui.navigation.LouArayaApp
import cl.louaraya.macrame.ui.theme.LouArayaMacrameTheme
import cl.louaraya.macrame.viewmodel.TiendaViewModel

/** Punto de entrada: crea el ViewModel y muestra la interfaz. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this)[TiendaViewModel::class.java]
        recibirAviso(intent)
        setContent { LouArayaMacrameTheme { LouArayaApp(viewModel) } }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recibirAviso(intent)
    }
    private fun recibirAviso(intent: Intent?) {
        val id = intent?.getStringExtra("aviso_id") ?: return
        val vm = ViewModelProvider(this)[TiendaViewModel::class.java]
        vm.avisoPendiente = id
        vm.correoAvisoPendiente = intent.getStringExtra("aviso_correo")
        intent.removeExtra("aviso_id")
        intent.removeExtra("aviso_correo")
    }

}
