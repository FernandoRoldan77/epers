package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.services.interfaces.PatogenoService
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.PatogenoDAO
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.eventos.MutacionEvento
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class PatogenoServiceImpl(private val patogenoH: PatogenoDAO, private val feedMongoImplDBDAO: FeedMongoImplDAO) : PatogenoService {
    override fun crearPatogeno(patogeno: Patogeno): Int {
        return TransactionRunner.runTrx {
            patogenoH.guardar(patogeno)
            if (patogeno.especies.isNotEmpty()) {
                val registro = MutacionEvento("Se ha agregado una especie al patogeno ${patogeno.tipo}")
                registro.agregarActorPatogeno(patogeno.tipo)
                feedMongoImplDBDAO.save(registro)
            }
            patogeno.id!!
        }
    }


    override fun agregarEspecie(id: Int, nombreEspecie: String, paisDeOrigen: String): Especie {
        return TransactionRunner.runTrx {
            val patogeno = patogenoH.recuperar(id)
            val especie = patogeno.crearEspecie(nombreEspecie, paisDeOrigen)
            patogenoH.actualizar(patogeno)
            val registro = MutacionEvento("Se ha agregado una especie al patogeno ${patogeno.tipo}")
            registro.agregarActorPatogeno(patogeno.tipo)
            feedMongoImplDBDAO.save(registro)
            especie
        }
    }

    override fun recuperarPatogenoPorTipo(tipo: String): Patogeno = TransactionRunner.runTrx { patogenoH.recuperarPorTipo(tipo) }
    override fun recuperarPatogenoId(id: Int): Patogeno = TransactionRunner.runTrx { patogenoH.recuperar(id) }
    override fun actualizarPatogeno(patogeno: Patogeno) = TransactionRunner.runTrx { patogenoH.actualizar(patogeno) }

    override fun recuperarEspecie(id: Int): Especie {
        return TransactionRunner.runTrx { patogenoH.recuperarEspecie(id) }
    }

    override fun cantidadDeInfectados(especieId: Int) = TransactionRunner.runTrx { patogenoH.cantidadDeInfectados(especieId) }

    override fun esPandemia(especieId: Int): Boolean {
        return TransactionRunner.runTrx { patogenoH.esPandemia(especieId) }
    }

    override fun recuperarTodosLosPatogenos(): List<Patogeno> = TransactionRunner.runTrx { patogenoH.recuperarATodos() }

    override fun eliminarTodosLosPatogenos() {
        TransactionRunner.runTrx { patogenoH.eliminarTodos() }
    }
}
