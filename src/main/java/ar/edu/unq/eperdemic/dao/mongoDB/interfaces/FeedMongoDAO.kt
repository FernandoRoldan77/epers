package ar.edu.unq.eperdemic.dao.mongoDB.interfaces

import ar.edu.unq.eperdemic.modelo.eventos.Evento

interface FeedMongoDAO {
    fun feedPatogeno(tipoDePatogeno: String) : List<Evento>
    fun feedVector(idVector: Int): List<Evento>
    fun feedUbicacion(ubicacionesLindantes: List<String>): List<Evento>
    fun save(anObject: Evento)
    fun obtenerPorTipoPatogeno(tipoDePatogeno: String): Evento?
    fun deleteAll()
}