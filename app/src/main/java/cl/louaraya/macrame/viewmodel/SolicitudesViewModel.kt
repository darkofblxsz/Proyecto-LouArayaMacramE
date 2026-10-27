package cl.louaraya.macrame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.louaraya.macrame.repository.SolicitudesLocal

class SolicitudesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SolicitudesLocal(application)
    fun todas() = repository.todas()
    fun crear(correo: String, productoId: Int, unidades: Int, fecha: String, comentario: String, personalizada: Boolean = false, color: String = "Original", material: String = "Original") =
        repository.crear(correo, productoId, unidades, fecha, comentario, personalizada, color, material)
    fun modificar(id: String, correo: String, unidades: Int, fecha: String, comentario: String, personalizada: Boolean, color: String, material: String) =
        repository.modificar(id, correo, unidades, fecha, comentario, personalizada, color, material)
    fun cambiarEstado(id: String, nuevo: String) = repository.cambiarEstado(id, nuevo)
}
