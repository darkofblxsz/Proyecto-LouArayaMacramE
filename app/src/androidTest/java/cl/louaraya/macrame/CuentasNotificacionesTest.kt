package cl.louaraya.macrame

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.louaraya.macrame.repository.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class CuentasNotificacionesTest {
    private lateinit var context: Context
    private lateinit var almacen: AlmacenLocal
    @Before fun preparar() {
        val prefijo = "cuentas_test_${UUID.randomUUID()}_"
        context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = super.getSharedPreferences(prefijo + name, mode)
        }
        almacen = AlmacenLocal(context)
    }
    @Test fun clienteNoPuedeCrearEditarNiEliminarCuentas() {
        assertTrue(almacen.cuentasAdmin("usuario@gmail.com").isEmpty())
        assertNotNull(almacen.guardarCuentaAdmin("usuario@gmail.com", null, "nuevo@gmail.com", "nuevo", "Nuevo", "", "", "Clave1234"))
        assertNotNull(almacen.eliminarCuentaAdmin("usuario@gmail.com", "admin@admin.com"))
    }
    @Test fun nuevoAdminTieneRolIndependienteDelDominio() {
        assertNull(almacen.guardarCuentaAdmin("admin@admin.com", null, "gestor@gmail.com", "gestor", "Gestor", "", "", "Clave1234"))
        assertEquals("ADMIN", AlmacenLocal(context).autenticar("gestor@gmail.com", "Clave1234"))
        assertNotNull(almacen.guardarCuentaAdmin("admin@admin.com", null, "gestor@gmail.com", "otro", "Otro", "", "", "Clave1234"))
    }
    @Test fun edicionConservaRolYCambiaClave() {
        assertNull(almacen.guardarCuentaAdmin("admin@admin.com", "usuario@gmail.com", "usuario@gmail.com", "cliente_editado", "Victoria", "González", "+56912345678", "Nueva1234"))
        assertNull(almacen.autenticar("usuario@gmail.com", "Usuario1234!"))
        assertEquals("USER", AlmacenLocal(context).autenticar("cliente_editado", "Nueva1234"))
    }
    @Test fun eliminacionNoSeRevierteAlReabrirNiBorraCuentaPropia() {
        assertNotNull(almacen.eliminarCuentaAdmin("admin@admin.com", "admin@admin.com"))
        assertNull(almacen.eliminarCuentaAdmin("admin@admin.com", "usuario@gmail.com"))
        assertNull(AlmacenLocal(context).perfil("usuario@gmail.com"))
        assertNotNull(almacen.registrar("usuario@gmail.com", "Clave1234", "Otro", "otro", "Apellido", "01/01/2000", "+56912345678"))
    }
    @Test fun pedidoActivoImpideEliminarCliente() {
        val repo = SolicitudesLocal(context)
        val fecha = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
        assertNull(repo.crear("usuario@gmail.com", almacen.productos().first().id, 1, fecha, ""))
        assertNotNull(almacen.eliminarCuentaAdmin("admin@admin.com", "usuario@gmail.com"))
        assertNull(repo.cambiarEstado(repo.todas().first().id, "Cancelado"))
        assertNull(almacen.eliminarCuentaAdmin("admin@admin.com", "usuario@gmail.com"))
    }
    @Test fun leerUnaNotificacionNoMarcaLasDemasNiOtraCuenta() {
        val repo = NotificacionesLocal(context)
        repo.agregar("usuario@gmail.com", "Primera", "Mensaje uno")
        repo.agregar("usuario@gmail.com", "Segunda", "Mensaje dos")
        val id = repo.todas("usuario@gmail.com").first().id
        repo.marcarLeida("admin@admin.com", id)
        assertEquals(2, repo.todas("usuario@gmail.com").count { !it.leida })
        repo.marcarLeida("usuario@gmail.com", id)
        assertEquals(1, NotificacionesLocal(context).todas("usuario@gmail.com").count { !it.leida })
    }
}
