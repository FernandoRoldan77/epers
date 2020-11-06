package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.dao.hibernate.impl.MutacionHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.EspecieDAO
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.MutacionDAO
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.modelo.entities.mutacion.Mutacion
import ar.edu.unq.eperdemic.modelo.eventos.MutacionEvento
import ar.edu.unq.eperdemic.services.interfaces.MutacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class MutacionServiceImpl(private val mutacionDAO: MutacionDAO, private val especieDAO: EspecieDAO,private val feedMongoImplDBDAO: FeedMongoImplDAO) : MutacionService {

    override fun crearMutacion(mutacion: Mutacion) {
        mutacion.id = TransactionRunner.runTrx { mutacionDAO.crear(mutacion) }
    }

    override fun recuperarMutacion(id: Int) = TransactionRunner.runTrx { mutacionDAO.recuperar(id) }
    override fun recuperarMutaciones(): List<Mutacion> = TransactionRunner.runTrx { mutacionDAO.recuperarMutaciones() }
    override fun actualizarMutacion(mutacion: Mutacion) = TransactionRunner.runTrx { mutacionDAO.actualizar(mutacion) }

    override fun eliminarTodaslasMutaciones() {
        TransactionRunner.runTrx {
            mutacionDAO.eliminarMutaciones()
        }
    }

    override fun mutar(especieId: Int, mutacionId: Int) {
        TransactionRunner.runTrx {
            val especieBase = especieDAO.recuperar(especieId)
            val mutacionBase = mutacionDAO.recuperar(mutacionId)
            mutacionBase.mutar(especieBase)

            val registro = MutacionEvento("La especie ${especieBase.nombre} del patogeno ${especieBase.patogeno.tipo} tiene una mutacion ${mutacionBase.nombre}")
            registro.agregarActorPatogeno(especieBase.patogeno.tipo)
            feedMongoImplDBDAO.save(registro)

            especieDAO.actualizar(especieBase)
        }
    }
}

