package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.VectorDAO
import ar.edu.unq.eperdemic.dao.mongoDB.interfaces.FeedMongoDAO
import ar.edu.unq.eperdemic.modelo.eventos.ContagioEvento
import ar.edu.unq.eperdemic.services.interfaces.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class VectorServiceImpl(private val vectorImplDao: VectorDAO, private val feedMongoImplDBDAO: FeedMongoDAO) : VectorService {
    override fun contagiar(vectorInfectado: Vector, vectores: List<Vector>) {
        TransactionRunner.runTrx {
            val vectoresNoInfectadosAntesDeContagio = vectores.filter { !it.estaContagiado }

            vectorInfectado.contagiarVectores(vectores.toMutableList())

            registrarUbicacionSiContagiado(vectoresNoInfectadosAntesDeContagio.filter { it.estaContagiado }, vectorInfectado)

            registrarUbicacionSiHayContagio(vectoresNoInfectadosAntesDeContagio.filter { it.estaContagiado }, vectorInfectado.ubicacion?.nombreUbicacion ?: "")

            vectorImplDao.actualizarVectores(vectores.toMutableList())
        }
    }

    private fun registrarUbicacionSiContagiado(nuevosVectoresContagiados: List<Vector>, vectorInfectado: Vector) {
        nuevosVectoresContagiados.forEach {
            val evento = ContagioEvento("El vector con id ${it.id} fue contagio por el vector con id: ${vectorInfectado.id}")
            evento.agregarActorVector(vectorInfectado.id!!)
            evento.agregarActorUbicacion(it.ubicacion?.nombreUbicacion ?: "")
            evento.agregarActorVectorContagiado(it.id!!)
            feedMongoImplDBDAO.save(evento)
        }
    }

    override fun infectar(vector: Vector, especie: Especie) {
        TransactionRunner.runTrx {
            vector.contraerEspecie(especie)

            val registro = ContagioEvento("El vector con id: ${vector.id} se infecto con ${especie.nombre}")
            registro.agregarActorVector(vector.id!!)
            feedMongoImplDBDAO.save(registro)

            registrarUbicacionSiHayContagio(listOf(vector),vector.ubicacion?.nombreUbicacion ?: "")

            vectorImplDao.actualizarVector(vector)


            val nombreUbicacion = vectorImplDao.nombreDeUbicacion(vector.id!!)

            val numero = cantidadDeEspeciesEnUbicacion(nombreUbicacion, especie.nombre)

            if (numero == 1 && nombreUbicacion != "") {
                val registro1 = ContagioEvento("La especie de patogeno ${especie.nombre} tiene su primer avistamiento en la ubicacion ${nombreUbicacion} ")
                registro1.agregarActorPatogeno(especie.patogeno.tipo)
                feedMongoImplDBDAO.save(registro1)
            }
        }
    }


    override fun enfermedades(vectorId: Int): List<Especie> = TransactionRunner.runTrx { vectorImplDao.recuperarEspecies(vectorId) }

    override fun crear(vector: Vector) = TransactionRunner.runTrx { vectorImplDao.guardar(vector) }
    override fun recuperarVector(vectorId: Int): Vector = TransactionRunner.runTrx { vectorImplDao.recuperarVector(vectorId) }
    override fun recuperarTodosVectores(): List<Vector> = TransactionRunner.runTrx { vectorImplDao.recuperarTodosVectores() }
    override fun recuperarVectorPorTipoBIologico(tipo: String): Vector = TransactionRunner.runTrx { vectorImplDao.recuperarPorTipo(tipo) }

    override fun borrarVector(vectorId: Int) = TransactionRunner.runTrx { vectorImplDao.borrarVector(vectorId) }
    override fun borrarTodos() = TransactionRunner.runTrx { vectorImplDao.borrarTodosVector() }
    override fun borrarTodasLasespecies() = TransactionRunner.runTrx { vectorImplDao.borrarTodasEspecies() }

    override fun actualizar(vector: Vector) {
        TransactionRunner.runTrx { vectorImplDao.actualizar(vector) }
   
    }

    override fun recuperar(id: Int): Vector {
        return TransactionRunner.runTrx { vectorImplDao.recuperar(id) }
    }


    fun cantidadDeEspeciesEnUbicacion(nombreUbicacion: String, nombreEspecie: String): Int {
        return vectorImplDao.cantidadDeEspeciesEnUbicacion(nombreUbicacion, nombreEspecie)
    }

    private fun registrarUbicacionSiHayContagio(vectoresNoInfectadosAntesDeContagio: List<Vector>, nombreUbicacion: String) {
        if (vectoresNoInfectadosAntesDeContagio.any { it.estaContagiado }) {
            val registroContagio = ContagioEvento("Agregada ubicacion con contagio ${nombreUbicacion} ")
            registroContagio.agregarActorUbicacion(nombreUbicacion)
            feedMongoImplDBDAO.save(registroContagio)
        }
    }

}