package ar.edu.unq.eperdemic.dao.mongoDB.impl

import ar.edu.unq.eperdemic.dao.mongoDB.generic.GenericMongoDAO
import ar.edu.unq.eperdemic.dao.mongoDB.interfaces.FeedMongoDAO
import ar.edu.unq.eperdemic.modelo.enums.TipoEvento
import ar.edu.unq.eperdemic.modelo.eventos.Evento
import com.mongodb.client.model.Filters.*

class FeedMongoImplDAO : GenericMongoDAO<Evento>(Evento::class.java), FeedMongoDAO {

    override fun obtenerPorTipoPatogeno(tipoDePatogeno: String): Evento? {
        return getBy("tipoPatogeno", tipoDePatogeno)
    }

    override fun feedPatogeno(tipoDePatogeno: String): List<Evento> {
        return find(
                and(`in`("tipoEvento", TipoEvento.Mutacion.name, TipoEvento.Contagio.name),
                        eq("tipoPatogeno", tipoDePatogeno))
        )
    }

    override fun feedVector(idVector: Int): List<Evento> {
        return find(
                and(`in`("tipoEvento", TipoEvento.Contagio.name, TipoEvento.Arribo.name),
                        eq("idVector", idVector))
        )
    }

    override fun feedUbicacion(ubicacionesLindantes: List<String>): List<Evento> {
        return find(
                and(`in`("tipoEvento", TipoEvento.Arribo.name, TipoEvento.Contagio.name),
                        `in`("nombreUbicacion", ubicacionesLindantes))
        )
    }
}
