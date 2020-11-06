package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.dao.mongoDB.interfaces.FeedMongoDAO
import ar.edu.unq.eperdemic.dao.neo4j.interfaces.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.eventos.Evento
import ar.edu.unq.eperdemic.modelo.eventos.MutacionEvento
import ar.edu.unq.eperdemic.services.interfaces.FeedService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class FeedServiceImpl(private val feedMongoDAO: FeedMongoDAO, private val ubicacionNeo4jDAO: UbicacionNeo4jDAO) : FeedService {

    override fun crearEventoAgregarEspecie(evento: MutacionEvento) {
        feedMongoDAO.save(evento)
    }

    override fun obtenerPorTipoPatogeno(tipoDePatogeno: String): Evento? {
        return feedMongoDAO.obtenerPorTipoPatogeno(tipoDePatogeno)
    }


    override fun feedUbicacion(nombreUbicacion: String): List<Evento> {
        return TransactionRunner.runTrx {
            val ubicacionesLindantes = ubicacionNeo4jDAO.conectados(nombreUbicacion).map {
                it.nombreUbicacion
            }.toMutableList()
            ubicacionesLindantes.add(nombreUbicacion)
            feedMongoDAO.feedUbicacion(ubicacionesLindantes)
        }
    }

    override fun feedVector(vectorId: Int): List<Evento> {
        return feedMongoDAO.feedVector(vectorId)
    }

    override fun feedPatogeno(tipoDePatogeno: String): List<Evento> {
        return feedMongoDAO.feedPatogeno(tipoDePatogeno)
    }


    override fun eliminarTodo() {
        feedMongoDAO.deleteAll()
    }

    fun eventosDePatogeno(tipoDeEvento: String, tipoDePatogeno: String): List<Evento> {
        return feedPatogeno(tipoDePatogeno).filter { it.tipoEvento == tipoDeEvento }
    }
}