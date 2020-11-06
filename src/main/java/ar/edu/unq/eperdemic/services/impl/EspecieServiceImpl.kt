package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.EspecieDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.services.interfaces.EspecieService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class EspecieServiceImpl(private val especieDao: EspecieDAO) : EspecieService {

    override fun crear(especie: Especie) {
        TransactionRunner.runTrx { especieDao.guardar(especie) }
    }

    override fun actualizar(especie: Especie) {
        return TransactionRunner.runTrx { especieDao.actualizar(especie) }
    }

    override fun recuperar(id: Int?): Especie {
        return TransactionRunner.runTrx { especieDao.recuperar(id) }
    }

    override fun recuperarTodos(): List<Especie> {
        return TransactionRunner.runTrx { especieDao.recuperarTodos() }
    }

    override fun eliminarTodos() {
        TransactionRunner.runTrx {
            especieDao.eliminarTodos()
        }
    }

}