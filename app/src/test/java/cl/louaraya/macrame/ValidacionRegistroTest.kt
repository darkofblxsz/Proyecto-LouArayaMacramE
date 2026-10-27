package cl.louaraya.macrame

import cl.louaraya.macrame.model.erroresRegistro
import org.junit.Assert.*
import org.junit.Test

class ValidacionRegistroTest {
    private fun validar(correo: String = "vicki@gmail.com", confirmar: String = "VICKI@gmail.com", clave: String = "Clave1234",
        repetir: String = "Clave1234", fecha: String = "15/05/2000", codigo: String = "+56", telefono: String = "912345678", terminos: Boolean = true) =
        erroresRegistro(correo, confirmar, clave, repetir, "Victoria", "González", fecha, "vicki", codigo, telefono, terminos)
    @Test fun aceptaDatosValidosYCorreoSinDistinguirMayusculas() { assertTrue(validar().isEmpty()) }
    @Test fun informaCadaErrorSimultaneamente() {
        val errores = validar(correo = "correo", confirmar = "otro", clave = "123", repetir = "456", codigo = "56", telefono = "123abc", terminos = false)
        assertTrue(errores.keys.containsAll(listOf("correo", "confirmarCorreo", "clave", "confirmarClave", "codigo", "telefono", "terminos")))
    }
    @Test fun rechazaFechasImposiblesYFuturas() {
        assertTrue(validar(fecha = "31/02/2000").containsKey("fecha"))
        assertTrue(validar(fecha = "01/01/2999").containsKey("fecha"))
        assertFalse(validar(fecha = "29/02/2000").containsKey("fecha"))
    }
    @Test fun dominioAdministradorReservado() { assertTrue(validar(correo = "alguien@ADMIN.COM").containsKey("correo")) }
    @Test fun vaciosMuestranTodosLosCampos() {
        val errores = erroresRegistro("", "", "", "", "", "", "", "", "", "", false)
        assertEquals(11, errores.size)
    }
}
