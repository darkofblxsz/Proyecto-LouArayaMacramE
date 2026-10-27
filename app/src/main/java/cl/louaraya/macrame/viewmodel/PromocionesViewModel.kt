package cl.louaraya.macrame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.louaraya.macrame.model.OfertaLocal
import cl.louaraya.macrame.repository.PromocionesLocal

class PromocionesViewModel(application: Application) : AndroidViewModel(application) {
    fun listar() = PromocionesLocal.listar(getApplication())
    fun activas(ahora: Long = System.currentTimeMillis()) = PromocionesLocal.activas(getApplication(), ahora)
    fun guardar(oferta: OfertaLocal) = PromocionesLocal.guardar(getApplication(), oferta)
    fun eliminar(id: Long) = PromocionesLocal.eliminar(getApplication(), id)
}
