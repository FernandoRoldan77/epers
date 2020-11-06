package ar.edu.unq.eperdemic.mongoDB

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.modelo.enums.TipoEvento
import ar.edu.unq.eperdemic.services.impl.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class VectorFeedTest {

    private val feedServiceImpl = FeedServiceImpl(FeedMongoImplDAO(), UbicacionNeo4jDAO())
    private val ubicacionServiceImpl = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorServiceImpl = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoServiceImpl = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(), FeedMongoImplDAO())

    val estadoMock: ProbabilidadDeContagio = Mockito.spy(ProbabilidadDeContagio::class.java)

    var vectorAnimal = Vector(Animal())
    var vectorInsecto = Vector(Insecto())
    var vectorHumano = Vector(Humano())

    var vectorInsecto2 = Vector(Insecto())
    var vectorHumano2 = Vector(Humano())

    val mg = ubicacionServiceImpl.crear("Monte Grande")
    val ezeiza = ubicacionServiceImpl.crear("Ezeiza")
    val wilde = ubicacionServiceImpl.crear("Wilde")
    val bernal = ubicacionServiceImpl.crear("Bernal")

    val covid = Especie(Patogeno("Virus"), "Covid", "china")
    val sars = Especie(Patogeno("Virus"), "Sars", "china")
    val sars2 = Especie(Patogeno("Virus"), "Sars2", "china")


    @Before
    fun setUp() {
        setDesignadorDeEstadoMock(vectorAnimal)
        setDesignadorDeEstadoMock(vectorHumano)
        setDesignadorDeEstadoMock(vectorInsecto)

        setDesignadorDeEstadoMock(vectorHumano2)
        setDesignadorDeEstadoMock(vectorInsecto2)

        especieServiceImpl.crear(covid)
        especieServiceImpl.crear(sars)
        especieServiceImpl.crear(sars2)

        ubicacionServiceImpl.conectar("Monte Grande", "Ezeiza", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Ezeiza", "Wilde", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Wilde", "Bernal", TipoCamino.Terrestre.name)



        vectorServiceImpl.crear(vectorAnimal)
        vectorServiceImpl.crear(vectorInsecto)

        vectorServiceImpl.crear(vectorHumano2)
        vectorServiceImpl.crear(vectorInsecto2)

        vectorHumano.moverAUbicacion(mg)
        vectorServiceImpl.crear(vectorHumano)
        vectorHumano.moverAUbicacion(ezeiza)
        ubicacionServiceImpl.mover(vectorHumano.id!!, ezeiza.nombreUbicacion)
        vectorHumano.moverAUbicacion(wilde)
        ubicacionServiceImpl.mover(vectorHumano.id!!, wilde.nombreUbicacion)
        vectorHumano.moverAUbicacion(bernal)
        ubicacionServiceImpl.mover(vectorHumano.id!!, bernal.nombreUbicacion)

        vectorServiceImpl.infectar(vectorHumano, covid)
        vectorServiceImpl.infectar(vectorHumano, sars)

        vectorServiceImpl.crear(vectorAnimal)
        vectorServiceImpl.infectar(vectorAnimal, sars2)
        vectorServiceImpl.contagiar(vectorAnimal, listOf(vectorHumano2, vectorInsecto2))
    }

    private fun setDesignadorDeEstadoMock(aVector: Vector) {
        Mockito.`when`(estadoMock.calcularFactorContagio(aVector.getTipoBiologico(), aVector.especies)).thenReturn(100)
        aVector.setProbabilidad(estadoMock)
    }

    @Test
    fun `todas las ubicaciones por donde se movie el vector`() {
        val eventosVector = feedServiceImpl.feedVector(vectorHumano.id!!).filter { it.tipoEvento == TipoEvento.Arribo.name }
        assert(
                listOf(ezeiza.nombreUbicacion, wilde.nombreUbicacion, bernal.nombreUbicacion).all { ubicacion ->
                    eventosVector.any { it.obtenerUbicacion() == ubicacion }
                }
        )
    }

    @Test
    fun `todas las ubicaciones por donde se movie el vector en orden cronologico descendiente`() {
        val eventosVector = feedServiceImpl.feedVector(vectorHumano.id!!).filter { it.tipoEvento == TipoEvento.Arribo.name }
        assertEquals(
                listOf(bernal.nombreUbicacion, wilde.nombreUbicacion, ezeiza.nombreUbicacion),
                eventosVector.map { it.obtenerUbicacion() }
        )
    }

    @Test
    fun `todas las enfermedades que tenga el vector`() {
        val eventosVector = feedServiceImpl.feedVector(vectorHumano.id!!).filter { it.tipoEvento == TipoEvento.Contagio.name }
        val especiesDelVector = eventosVector.flatMap { it ->
            vectorServiceImpl.recuperarVector(it.obtenerVector()).especies.map { it.nombre }
        }.distinct()

        assert(
                listOf(covid.nombre, sars.nombre).all { especie ->
                    especiesDelVector.any { it == especie }
                }
        )
    }

    @Test
    fun `todas las enfermedades que tenga el vector en orden cronologico descendiente`() {
        val eventosVector = feedServiceImpl.feedVector(vectorHumano.id!!).filter { it.tipoEvento == TipoEvento.Contagio.name }
        assertEquals(
                listOf(covid.nombre, sars.nombre),
                eventosVector.flatMap { it ->
                    vectorServiceImpl.recuperarVector(it.obtenerVector()).especies.map { it.nombre }
                }.distinct()
        )
    }

    @Test
    fun `vector animal contagio a vectores humano e insecto`() {
        val eventosVector = feedServiceImpl.feedVector(vectorAnimal.id!!).filter {
            it.tipoEvento == TipoEvento.Contagio.name
                    && it.obtenerVectorContagiado() != 0
        }
        assert(listOf(vectorHumano2.id, vectorInsecto2.id).all {
            eventosVector.any { e -> e.obtenerVectorContagiado() == it }
        })
    }

    @Test
    fun `vector animal contagio a vectores humano e insecto con orden cronologico descendiente`() {
        val eventosVector = feedServiceImpl.feedVector(vectorAnimal.id!!).filter {
            it.tipoEvento == TipoEvento.Contagio.name
                    && it.obtenerVectorContagiado() != 0
        }
        assertEquals(
                listOf(vectorInsecto2.id, vectorHumano2.id),
                eventosVector.map { it.obtenerVectorContagiado() }
        )
    }

    @After
    fun eliminarTodo() {
        vectorServiceImpl.borrarTodasLasespecies()
        vectorServiceImpl.borrarTodos()
        ubicacionServiceImpl.eliminarTodasLasUbicaciones()
        especieServiceImpl.eliminarTodos()
        patogenoServiceImpl.eliminarTodosLosPatogenos()
        feedServiceImpl.eliminarTodo()
    }
}