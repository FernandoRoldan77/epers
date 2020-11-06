package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.PatogenoDAO
import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.UbicacionDAO
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.VectorDAO
import ar.edu.unq.eperdemic.dao.mongoDB.interfaces.FeedMongoDAO
import ar.edu.unq.eperdemic.dao.neo4j.interfaces.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.eventos.ArriboEvento
import ar.edu.unq.eperdemic.modelo.eventos.ContagioEvento
import ar.edu.unq.eperdemic.services.interfaces.UbicacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class UbicacionServiceImpl(private var ubicacionHibernateDAO: UbicacionDAO,
                           private var ubicacionNeo4jDAO: UbicacionNeo4jDAO,
                           private var vectorDao: VectorDAO,
                           private val feedMongoDAO: FeedMongoDAO,
                           private val patogenoDAO: PatogenoDAO) : UbicacionService {

    override fun mover(vectorId: Int, nombreUbicacion: String) {
        TransactionRunner.runTrx {

            val ubicacionDestino = ubicacionHibernateDAO.recuperarPorNombre(nombreUbicacion)
            val vectorAMover = vectorDao.recuperar(vectorId)
            val ubicacionVectorAMover = vectorAMover.ubicacion!!

            val vectoresNoInfectadosAntesDeContagio = ubicacionDestino.vectores.filter { !it.estaContagiado }

            validarMovimientoEntreUibicaciones(ubicacionVectorAMover, ubicacionDestino, vectorAMover.obtenerCaminosPosibles())

            vectorAMover.contagiarVectores(ubicacionDestino.vectores)
            vectorAMover.moverAUbicacion(ubicacionDestino)


            val registro = ArriboEvento("El vector con id: ${vectorAMover.id} arribo a la ubicacion ${nombreUbicacion}")
            registro.agregarActorVector(vectorAMover.id!!)
            registro.agregarActorUbicacion(ubicacionDestino.nombreUbicacion)
            feedMongoDAO.save(registro)


            registrarNuevosVectoresContagiados(vectoresNoInfectadosAntesDeContagio.filter { it.estaContagiado }, vectorAMover)


            registrarMovimientoAUnaUbicacion(ubicacionDestino)


            registrarUbicacionContagiada(vectoresNoInfectadosAntesDeContagio, ubicacionDestino)

            ubicacionHibernateDAO.actualizar(ubicacionDestino)
            registarEventoDePrimerAvistamientoDeEspeciePorVector(vectorAMover)


            verificarSiHayPandemia(vectorAMover)
        }
    }

    private fun verificarSiHayPandemia(vectorAMover: Vector) {
        vectorAMover.especies.forEach {
            if (patogenoDAO.esPandemia(it.id!!)) {
                registrarEventoEsPandemia(it)
            }
        }
    }

    private fun registrarEventoEsPandemia(especiePandemia: Especie) {
        val registroContagio = ContagioEvento("La especie ${especiePandemia.nombre} perteneciente al patogeno " +
                "${especiePandemia.patogeno.tipo} se ha convertido en pandemia")

        registroContagio.agregarActorPatogeno(especiePandemia.patogeno.tipo)
        feedMongoDAO.save(registroContagio)
    }

    private fun registrarMovimientoAUnaUbicacion(ubicacionDestino: Ubicacion) {

        val registroArribo = ArriboEvento("Se agrego una ubicacion.${ubicacionDestino.nombreUbicacion}")
        registroArribo.agregarActorUbicacion(ubicacionDestino.nombreUbicacion)
        feedMongoDAO.save(registroArribo)
    }

    private fun registrarUbicacionContagiada(vectoresNoInfectadosAntesDeContagio: List<Vector>, ubicacionDestino: Ubicacion) {
        if (vectoresNoInfectadosAntesDeContagio.any { it.estaContagiado }) {
            val registroContagio = ContagioEvento("Agregada ubicacion con contagio${ubicacionDestino.nombreUbicacion} ")
            registroContagio.agregarActorUbicacion(ubicacionDestino.nombreUbicacion)
            feedMongoDAO.save(registroContagio)

        }
    }

    private fun validarMovimientoEntreUibicaciones(ubicacionActual: Ubicacion,
                                                   ubicacionDestino: Ubicacion,
                                                   caminosPosibles: List<String>) {

        ubicacionActual.esUbicacionMuyLejana(
                ubicacionNeo4jDAO.esUbicacionMuyLejana(
                        ubicacionActual.nombreUbicacion,
                        ubicacionDestino.nombreUbicacion
                ),
                ubicacionDestino.nombreUbicacion
        )
        ubicacionActual.esUbicacionAlcanzable(
                ubicacionNeo4jDAO.esUbicacionAlcanzable(
                        ubicacionActual.nombreUbicacion,
                        ubicacionDestino.nombreUbicacion,
                        caminosPosibles
                )
        )
    }

    override fun esUbicacionAlcanzable(ubicacionOrigen: String, ubicacionDestino: String, caminos: List<String>): Boolean {
        return TransactionRunner.runTrx {
            ubicacionNeo4jDAO.esUbicacionAlcanzable(ubicacionOrigen, ubicacionDestino, caminos)
        }
    }

    override fun recuperarPorNombre(nombreUbicacion: String): Ubicacion {
        return TransactionRunner.runTrx {
            val ubicacion = ubicacionHibernateDAO.recuperarPorNombre(nombreUbicacion)
            ubicacion
        }
    }

    override fun expandir(nombreUbicacion: String) {
        TransactionRunner.runTrx {
            val ubicacionBase = ubicacionHibernateDAO.recuperarPorNombre(nombreUbicacion)
            ubicacionBase.vectores.forEach { it.setProbabilidad(ProbabilidadDeContagio()) }
            val vectorContagiadoAlAzar = ubicacionBase.vectores.filter { it.estaContagiado }.random()

            val vectoresNoInfectadosAntesDeContagio = ubicacionBase.vectores.filter { !it.estaContagiado }

            vectorContagiadoAlAzar.contagiarVectoresPorVectorRandom(ubicacionBase.vectores.minus(vectorContagiadoAlAzar))
            registrarNuevosVectoresContagiados(vectoresNoInfectadosAntesDeContagio.filter { it.estaContagiado }, vectorContagiadoAlAzar)
            registrarMovimientoAUnaUbicacion(ubicacionBase)
            registrarUbicacionContagiada(vectoresNoInfectadosAntesDeContagio, ubicacionBase)

            ubicacionHibernateDAO.actualizar(ubicacionBase)
        }
    }

    override fun crear(nombreUbicacion: String): Ubicacion {
        val ubicacion = Ubicacion()
        ubicacion.nombreUbicacion = nombreUbicacion
        TransactionRunner.runTrx {
            ubicacion.id = ubicacionHibernateDAO.crear(ubicacion)
            ubicacionNeo4jDAO.crear(nombreUbicacion)
        }
        return ubicacion
    }

    override fun actualizarUbicacion(ubicacion: Ubicacion) = TransactionRunner.runTrx { ubicacionHibernateDAO.actualizar(ubicacion) }
    override fun recuperar(id: Int) = TransactionRunner.runTrx { ubicacionHibernateDAO.recuperar(id) }
    override fun eliminarUbicacion(ubicacion: Ubicacion) = TransactionRunner.runTrx { ubicacionHibernateDAO.eliminarUbicacion(ubicacion) }
    override fun recuperarTodasLasUbicaciones(): List<Ubicacion> = TransactionRunner.runTrx { ubicacionHibernateDAO.recuperarATodos() }
    override fun eliminarTodasLasUbicaciones() = TransactionRunner.runTrx {
        ubicacionHibernateDAO.eliminarUbicaciones()
        ubicacionNeo4jDAO.eliminarTodo()
    }

    override fun conectar(ubicacion1: String, ubicacion2: String, tipoCamino: String) {
        TransactionRunner.runTrx {
            ubicacionNeo4jDAO.conectarUbicaciones(ubicacion1, ubicacion2, tipoCamino)
        }
    }

    override fun conectados(nombreDeUbicacion: String): List<Ubicacion> {
        return TransactionRunner.runTrx {
            ubicacionNeo4jDAO.conectados(nombreDeUbicacion)
        }
    }

    override fun moverMasCorto(vectorId: Int, nombreDeUbicacion: String) {
        TransactionRunner.runTrx {

            val vector = vectorDao.recuperarVector(vectorId)
            val canminosPosiblesVector = vector.obtenerCaminosPosibles()
            val ubicacionesARecorrerParaMover = obtenerUbicacionesCaminoMasCorto(vector, nombreDeUbicacion)
            val vectoresNoInfectadosAntesDeContagio = vector.ubicacion!!.vectores.filter { !it.estaContagiado }

            if (ubicacionesARecorrerParaMover.isNotEmpty()) {
                ubicacionesARecorrerParaMover.forEach {
                    val ubicacionVectorAMover = vector.ubicacion!!

                    validarMovimientoEntreUibicaciones(ubicacionVectorAMover, it, canminosPosiblesVector)

                    vector.moverAUbicacion(it)
                }
                vector.contagiarVectores(vector.ubicacion!!.vectores)

                val registro = ArriboEvento("El vector con id: ${vector.id} arribo a la ubicacion ${nombreDeUbicacion}")
                registro.agregarActorVector(vector.id!!)
                registro.agregarActorUbicacion(vector.ubicacion?.nombreUbicacion ?: "")
                feedMongoDAO.save(registro)
                registrarNuevosVectoresContagiados(vectoresNoInfectadosAntesDeContagio.filter { it.estaContagiado }, vector)
                registrarMovimientoAUnaUbicacion(vector.ubicacion!!)
                registrarUbicacionContagiada(vectoresNoInfectadosAntesDeContagio, vector.ubicacion!!)
                vectorDao.actualizar(vector)
                verificarSiHayPandemia(vector)
            }
        }
    }

    private fun registrarNuevosVectoresContagiados(nuevosVectoresContagiados: List<Vector>, vectorInfectado: Vector) {
        nuevosVectoresContagiados.forEach {
            val evento = ContagioEvento("El vector con id ${it.id} fue contagio por el vector con id: ${vectorInfectado.id}")
            evento.agregarActorVector(vectorInfectado.id!!)
            evento.agregarActorVectorContagiado(it.id!!)
            feedMongoDAO.save(evento)
        }
    }

    private fun obtenerUbicacionesCaminoMasCorto(vector: Vector, nombreDeUbicacionDestino: String): List<Ubicacion> {
        return ubicacionNeo4jDAO.caminoMasCorto(vector.ubicacion!!.nombreUbicacion, nombreDeUbicacionDestino).filter {
            it.nombreUbicacion != vector.ubicacion!!.nombreUbicacion
        }.map { ubicacionHibernateDAO.recuperarPorNombre(it.nombreUbicacion) }
    }

    override fun caminoEntreDosUbicacionesLindantes(nombreDeUbicacionOrigen: String, nombredeUbicacionDestino: String): String =
            TransactionRunner.runTrx { ubicacionNeo4jDAO.getCaminoEntreDosUbicacionesLindantes(nombreDeUbicacionOrigen, nombredeUbicacionDestino) }


    override fun cantidadaDeCaminosPorTipoDeCamino(nombreDeLaUbicacionIngresada: String, tipoDeCaminoIngresado: String): Int =
            TransactionRunner.runTrx { ubicacionNeo4jDAO.cantidadDeCaminosDeTipo(nombreDeLaUbicacionIngresada, tipoDeCaminoIngresado) }

    override fun capacidadDeExpansion(vectorId: Int, movimientos: Int): Int {
        val vector = TransactionRunner.runTrx { vectorDao.recuperarVector(vectorId) }
        val res = TransactionRunner.runTrx {
            ubicacionNeo4jDAO.cantidadDeCaminosPosiblesSegunMovientos(vector.ubicacion!!.nombreUbicacion, vector.obtenerCaminosPosibles(), movimientos)
        }
        return res
    }

    override fun costoDeCaminoMasCorto(nombreUbicacion: String, tipoDeCaminoIngresado: String): Int =
            TransactionRunner.runTrx { ubicacionNeo4jDAO.cantidadDeCaminoMasCorto(nombreUbicacion, tipoDeCaminoIngresado) }

    override fun esUbicacionMuyLejana(ubicacionOrigen: String,
                                      ubicacionDestino: String): Boolean {
        return TransactionRunner.runTrx {
            ubicacionNeo4jDAO.esUbicacionMuyLejana(
                    ubicacionOrigen,
                    ubicacionDestino
            )
        }
    }


    override fun cantidadDeEspeciesEnUnaUbicacion(nombreUbicacion: String, nombreEspecie: String): Int = ubicacionHibernateDAO.cantidadDeEspeciesEnUbicacion(nombreUbicacion, nombreEspecie)
    fun registarEventoDePrimerAvistamientoDeEspeciePorVector(vector: Vector) {
        val especies = vector.especies
        val nombreUbicacion = vector.ubicacion!!.nombreUbicacion
        especies.forEach { crearEventoSiEsPrimerAvistamientoDeEspecie(it, nombreUbicacion) }
    }

    private fun crearEventoSiEsPrimerAvistamientoDeEspecie(especie: Especie, nombreUbicacion: String) {
        val cant = cantidadDeEspeciesEnUnaUbicacion(nombreUbicacion, especie.nombre)
        if (cant == 1) {
            val registro = ContagioEvento("La especie de patogeno ${especie.nombre} tiene su primer avistamiento en la ubicacion ${nombreUbicacion} ")
            registro.agregarActorPatogeno(especie.patogeno.tipo)
            feedMongoDAO.save(registro)
        }
    }


}

