package cl.louaraya.macrame

import cl.louaraya.macrame.model.*
import cl.louaraya.macrame.repository.*
import cl.louaraya.macrame.ui.components.*

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SolicitudesIntegrationTest {
    private lateinit var context: Context
    private lateinit var almacen: AlmacenLocal
    private lateinit var repo: SolicitudesLocal
    private val fecha get() = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))

    @Before fun preparar() {
        val prefijo = "test_${UUID.randomUUID()}_"
        context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                super.getSharedPreferences(prefijo + name, mode)
        }
        almacen = AlmacenLocal(context)
        repo = SolicitudesLocal(context)
    }

    @Test fun confirmarYCancelarRestituyeStockUnaSolaVez() {
        val p = almacen.productos().first()
        assertNull(repo.crear("usuario@gmail.com", p.id, 2, fecha, "Prueba"))
        val id = repo.todas().single().id
        assertEquals(p.stock, almacen.productos().first().stock)
        assertNull(repo.cambiarEstado(id, "Confirmado"))
        assertEquals(p.stock - 2, almacen.productos().first().stock)
        assertNotNull(repo.cambiarEstado(id, "Confirmado"))
        assertNull(repo.cambiarEstado(id, "Cancelado"))
        assertEquals(p.stock, almacen.productos().first().stock)
        assertNotNull(repo.cambiarEstado(id, "Cancelado"))
    }

    @Test fun entregarNoDescuentaDosVecesYSeConservaAlReabrir() {
        val p = almacen.productos().first()
        assertNull(repo.crear("usuario@gmail.com", p.id, 1, fecha, ""))
        val id = repo.todas().single().id
        assertNull(repo.cambiarEstado(id, "Confirmado"))
        assertNull(repo.cambiarEstado(id, "Entregado"))
        assertEquals(p.stock - 1, almacen.productos().first().stock)
        assertEquals("Entregado", SolicitudesLocal(context).todas().single().estado)
    }

    @Test fun rechazaCantidadFechaCuentaYStockInsuficientes() {
        val p = almacen.productos().first()
        assertNotNull(repo.crear("", p.id, 1, fecha, ""))
        assertNotNull(repo.crear("usuario@gmail.com", p.id, 0, fecha, ""))
        assertNotNull(repo.crear("usuario@gmail.com", p.id, p.stock + 1, fecha, ""))
        assertNotNull(repo.crear("usuario@gmail.com", p.id, 1, "31/02/2027", ""))
        assertTrue(repo.todas().isEmpty())
        assertNull(repo.crear("usuario@gmail.com", p.id, p.stock, fecha, ""))
        val id = repo.todas().single().id
        almacen.guardarProducto(p.copy(stock = 0))
        assertNotNull(repo.cambiarEstado(id, "Confirmado"))
        assertEquals("Pendiente", repo.todas().single().estado)
    }

    @Test fun fotosSeConservanAlEditarYReservarStock() {
        val p = almacen.productos().first().copy(fotos = listOf("foto1.jpg", "foto2.jpg"))
        almacen.guardarProducto(p)
        assertNull(repo.crear("usuario@gmail.com", p.id, 1, fecha, ""))
        assertNull(repo.cambiarEstado(repo.todas().single().id, "Confirmado"))
        assertEquals(p.fotos, AlmacenLocal(context).productos().first().fotos)
    }

    @Test fun qrSoloAdmiteCodigosYNoEnlacesExternos() {
        assertEquals("MAC-001", codigoDesdeQr("LOUARAYA:MAC-001"))
        assertEquals("MAC-002", codigoDesdeQr(" mac-002 "))
        assertNull(codigoDesdeQr("https://example.com/MAC-001"))
        assertNull(codigoDesdeQr("texto desconocido"))
    }
}
