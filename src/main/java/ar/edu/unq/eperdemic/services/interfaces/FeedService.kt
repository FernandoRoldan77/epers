package ar.edu.unq.eperdemic.services.interfaces

import ar.edu.unq.eperdemic.modelo.eventos.Evento
import ar.edu.unq.eperdemic.modelo.eventos.MutacionEvento

interface FeedService {
    fun crearEventoAgregarEspecie(evento: MutacionEvento)
    fun feedUbicacion(nombreUbicacion: String): List<Evento>
    fun obtenerPorTipoPatogeno(tipoDePatogeno: String): Evento?
    fun feedVector(vectorId: Int): List<Evento>
    fun feedPatogeno(tipoDePatogeno: String):List<Evento>
    fun eliminarTodo()
}
