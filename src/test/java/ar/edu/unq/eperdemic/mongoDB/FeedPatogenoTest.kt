package ar.edu.unq.eperdemic.mongoDB

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.mutacion.MutacionCombinada
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.modelo.enums.TipoDeVector
import ar.edu.unq.eperdemic.modelo.enums.TipoEvento
import ar.edu.unq.eperdemic.services.impl.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class FeedPatogenoTest {
    private val feedServiceImpl = FeedServiceImpl(FeedMongoImplDAO(), UbicacionNeo4jDAO())
    private val ubicacionServiceImpl = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorServiceImpl = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoServiceImpl = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(), FeedMongoImplDAO())

    val probabilidadDeContagioMock: ProbabilidadDeContagio = Mockito.spy(ProbabilidadDeContagio::class.java)

    var vectorAnimal = Vector(Animal())
    var vectorInsecto = Vector(Insecto())
    var vectorHumano = Vector(Humano())

    val mg = ubicacionServiceImpl.crear("Monte Grande")
    val ezeiza = ubicacionServiceImpl.crear("Ezeiza")
    val wilde = ubicacionServiceImpl.crear("Wilde")
    val bernal = ubicacionServiceImpl.crear("Bernal")

    val covid = Patogeno("Virus")
    val sars = Patogeno("Virus2")
    val sars2 = Patogeno("Virus3")

    val vectorAnimalPandemico = Vector(Animal())
    val vectorAnimalPandemicoAuxiliar = Vector(Animal())
    val vectorAnimalPandemicoAuxiliar2 = Vector(Animal())
    val idPatogenoProtozo = patogenoServiceImpl.crearPatogeno(Patogeno("Protozo"))

    @Before
    fun setUp() {
        val idCovid = patogenoServiceImpl.crearPatogeno(covid)
        val idSars = patogenoServiceImpl.crearPatogeno(sars)
        patogenoServiceImpl.crearPatogeno(sars2)
        val covidBD = patogenoServiceImpl.agregarEspecie(idCovid, "covid", "China")
        patogenoServiceImpl.agregarEspecie(idCovid, "sars", "China")
        val sarsBD = patogenoServiceImpl.agregarEspecie(idCovid, "sars", "China")
        val sarsX = patogenoServiceImpl.agregarEspecie(idSars, "sarsx", "China")
        val mutacion = MutacionCombinada("tos", TipoDeVector.Insecto, 0)


        mutacionService.crearMutacion(mutacion)

        ubicacionServiceImpl.conectar("Monte Grande", "Ezeiza", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Ezeiza", "Monte Grande", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Ezeiza", "Wilde", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Wilde", "Bernal", TipoCamino.Terrestre.name)


        vectorServiceImpl.crear(vectorAnimal)
        vectorServiceImpl.crear(vectorInsecto)

        vectorHumano.moverAUbicacion(mg)
        vectorAnimal.moverAUbicacion(mg)
        vectorServiceImpl.crear(vectorHumano)
        vectorHumano.moverAUbicacion(ezeiza)
        ubicacionServiceImpl.mover(vectorHumano.id!!, ezeiza.nombreUbicacion)
        vectorHumano.moverAUbicacion(wilde)
        ubicacionServiceImpl.mover(vectorHumano.id!!, wilde.nombreUbicacion)
        vectorHumano.moverAUbicacion(bernal)
        ubicacionServiceImpl.mover(vectorHumano.id!!, bernal.nombreUbicacion)
        setDesignadorDeEstadoMock(vectorAnimal)
        setDesignadorDeEstadoMock(vectorHumano)
        setDesignadorDeEstadoMock(vectorInsecto)

        vectorServiceImpl.infectar(vectorHumano, covidBD)
        vectorServiceImpl.infectar(vectorHumano, sarsBD)


        setDesignadorDeEstadoMock(vectorAnimalPandemico)
        setDesignadorDeEstadoMock(vectorAnimalPandemicoAuxiliar)
        setDesignadorDeEstadoMock(vectorAnimalPandemicoAuxiliar2)

        vectorServiceImpl.crear(vectorAnimal)
        vectorServiceImpl.infectar(vectorAnimal, sarsX)
        vectorServiceImpl.contagiar(vectorAnimal, listOf(vectorHumano, vectorInsecto))

        ////Mutar

        mutacionService.mutar(covidBD.id!!, mutacion.id!!)
        ubicacionServiceImpl.mover(vectorAnimal.id!!, "Ezeiza")


    }

    private fun setDesignadorDeEstadoMock(aVector: Vector) {
        Mockito.`when`(probabilidadDeContagioMock.calcularFactorContagio(aVector.getTipoBiologico(), aVector.especies)).thenReturn(100)
        Mockito.`when`(probabilidadDeContagioMock.esContagioExitoso(aVector)).thenReturn(true)
        aVector.setProbabilidad(probabilidadDeContagioMock)
    }

    @Test
    fun `cada vez que se muta una especie del patogeno dado`() {

        val eventosPatogeno = feedServiceImpl.eventosDePatogeno(TipoEvento.Mutacion.name, "Virus")
        assertEquals(
                listOf("tos"),
                eventosPatogeno.map {
                    patogenoServiceImpl.recuperarPatogenoPorTipo(it.obtenerPatogeno())
                }.distinctBy { it.id }.flatMap { it.especies }.flatMap { it.mutaciones }.map { it.nombre })
    }

    @Test
    fun `se genera un evento cuando una especie se encuentra por primera vez en una ubicacion`() {

        val eventosPatogeno = feedServiceImpl.eventosDePatogeno(TipoEvento.Contagio.name, "Virus")
        assertEquals(
                listOf("covid", "sars"),
                eventosPatogeno.map {
                    patogenoServiceImpl.recuperarPatogenoPorTipo(it.obtenerPatogeno())
                }.distinctBy { it.id }.flatMap { it.especies }.distinctBy { it.nombre }.map { it.nombre }
        )
    }

    @Test
    fun `se genera un evento si el vector infectado se mueve a una ubicacion donde no se tienen registros de la especies de dicho vector`() {
        val eventosPatogeno = feedServiceImpl.eventosDePatogeno(TipoEvento.Contagio.name, "Virus2")
        assertEquals(
                2,
                eventosPatogeno.size
        )
    }

    @Test
    fun `se crearon tres especies del patogeno virus`() {

        val eventosPatogeno = feedServiceImpl.eventosDePatogeno(TipoEvento.Mutacion.name, "Virus")

        assertEquals(
                listOf("covid", "sars", "sars"),
                eventosPatogeno.map {
                    patogenoServiceImpl.recuperarPatogenoPorTipo(it.obtenerPatogeno())
                }.distinctBy { it.id }.flatMap { it.especies }.map { it.nombre }
        )
    }

    @Test
    fun `especie es pandemia mover mas corto`() {
        vectorAnimalPandemico.moverAUbicacion(ezeiza)
        vectorAnimalPandemicoAuxiliar.moverAUbicacion(ezeiza)
        vectorAnimalPandemicoAuxiliar2.moverAUbicacion(wilde)

        vectorServiceImpl.crear(vectorAnimalPandemico)
        vectorServiceImpl.crear(vectorAnimalPandemicoAuxiliar)
        vectorServiceImpl.crear(vectorAnimalPandemicoAuxiliar2)

        val tos = patogenoServiceImpl.agregarEspecie(idPatogenoProtozo, "tos", "China")

        vectorServiceImpl.infectar(vectorAnimalPandemico, tos)
        vectorAnimalPandemico.contraerEspecie(tos)

        vectorServiceImpl.infectar(vectorAnimalPandemicoAuxiliar, tos)
        vectorAnimalPandemicoAuxiliar.contraerEspecie(tos)

        vectorServiceImpl.infectar(vectorAnimalPandemicoAuxiliar2, tos)
        vectorAnimalPandemicoAuxiliar2.contraerEspecie(tos)

        vectorServiceImpl.actualizar(vectorAnimalPandemico)
        vectorServiceImpl.actualizar(vectorAnimalPandemicoAuxiliar)
        vectorServiceImpl.actualizar(vectorAnimalPandemicoAuxiliar2)

        ubicacionServiceImpl.moverMasCorto(vectorAnimalPandemico.id!!, mg.nombreUbicacion)

        val eventosPatogeno = feedServiceImpl.eventosDePatogeno(TipoEvento.Contagio.name, "Protozo")

        assert(eventosPatogeno.any { it.descripcion.contains("La especie tos perteneciente al patogeno Protozo se ha convertido en pandemia") })

    }

    @Test
    fun `especie es pandemia mover`() {
        vectorAnimalPandemico.moverAUbicacion(ezeiza)
        vectorAnimalPandemicoAuxiliar.moverAUbicacion(ezeiza)
        vectorAnimalPandemicoAuxiliar2.moverAUbicacion(wilde)

        vectorServiceImpl.crear(vectorAnimalPandemico)
        vectorServiceImpl.crear(vectorAnimalPandemicoAuxiliar)
        vectorServiceImpl.crear(vectorAnimalPandemicoAuxiliar2)

        val tos = patogenoServiceImpl.agregarEspecie(idPatogenoProtozo, "tos", "China")

        vectorServiceImpl.infectar(vectorAnimalPandemico, tos)
        vectorAnimalPandemico.contraerEspecie(tos)

        vectorServiceImpl.infectar(vectorAnimalPandemicoAuxiliar, tos)
        vectorAnimalPandemicoAuxiliar.contraerEspecie(tos)

        vectorServiceImpl.infectar(vectorAnimalPandemicoAuxiliar2, tos)
        vectorAnimalPandemicoAuxiliar2.contraerEspecie(tos)

        vectorServiceImpl.actualizar(vectorAnimalPandemico)
        vectorServiceImpl.actualizar(vectorAnimalPandemicoAuxiliar)
        vectorServiceImpl.actualizar(vectorAnimalPandemicoAuxiliar2)

        ubicacionServiceImpl.mover(vectorAnimalPandemico.id!!, mg.nombreUbicacion)

        val eventosPatogeno = feedServiceImpl.eventosDePatogeno(TipoEvento.Contagio.name, "Protozo")

        assert(eventosPatogeno.any { it.descripcion.contains("La especie tos perteneciente al patogeno Protozo se ha convertido en pandemia") })
    }

    @After
    fun eliminarTodo() {
        feedServiceImpl.eliminarTodo()
        vectorServiceImpl.borrarTodasLasespecies() // elimina todas las especies asociadas a los vectores
        vectorServiceImpl.borrarTodos()
        ubicacionServiceImpl.eliminarTodasLasUbicaciones()
        especieServiceImpl.eliminarTodos()
        patogenoServiceImpl.eliminarTodosLosPatogenos()
        mutacionService.eliminarTodaslasMutaciones()
    }
}