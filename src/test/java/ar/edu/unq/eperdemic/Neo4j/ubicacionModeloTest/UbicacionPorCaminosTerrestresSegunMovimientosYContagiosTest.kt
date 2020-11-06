@file:Suppress("DEPRECATION")

package ar.edu.unq.eperdemic.Neo4j.ubicacionModeloTest

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.modelo.exception.UbicacionNoAlcanzableException
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class UbicacionPorCaminosTerrestresSegunMovimientosYContagiosTest {


    var humano: Humano = Humano()
    private val animal = Animal()
    private var vectorAnimal = Vector(animal)
    private val insecto = Insecto()
    private var vectorInsecto = Vector(insecto)
    private var vectorHumano = Vector(humano)

    private val ubicacionService = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorService = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoService = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieService = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(),FeedMongoImplDAO())
    private val dataServiceHibernate = DataServiceHibernate(ubicacionService, vectorService, patogenoService, especieService, mutacionService)


    @Before
    fun crearSetDeDatosIniciales() {
        dataServiceHibernate.crearSetDeDatosIniciales()
        ubicacionService.crear("Quilmes")

        vectorHumano.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorHumano)

        vectorInsecto.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorInsecto)

        vectorAnimal.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorAnimal)
        ubicacionService.conectar("Lanus", "Glew", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Lanus", "Quilmes", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Quilmes", "Avellaneda", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Glew", "Avellaneda", TipoCamino.Terrestre.name)


    }

    @Test
    fun DadaUnaUbicacionSeRetornanTodasLasUbicacionesConectadasPorUnCaminoTerrestre() {
        val tipoDeCaminoIngresado = TipoCamino.Terrestre.name
        ubicacionService.conectar("Lanus", "Glew", tipoDeCaminoIngresado)
        ubicacionService.conectar("Lanus", "Quilmes", tipoDeCaminoIngresado)
        val nombreDeLaUbicacionIngresada = "Lanus"
        val cantidadDeCaminosTerrestresConectados = 2
        val cantidadDeCaminosDeObtenidos = ubicacionService.cantidadaDeCaminosPorTipoDeCamino(nombreDeLaUbicacionIngresada, tipoDeCaminoIngresado)
        assert(cantidadDeCaminosTerrestresConectados == cantidadDeCaminosDeObtenidos)
    }


    @Test
    fun CrearUnaUbicacionQuePuedaConectarseAOtraUbicacionPorUnCaminoTerrestre() {

        val tipoDeCaminoIngresado = TipoCamino.Terrestre.name
        val tipoDeCaminoEntreLanusYQuilmes = ubicacionService.caminoEntreDosUbicacionesLindantes("Lanus", "Quilmes")

        assertTrue(tipoDeCaminoIngresado == tipoDeCaminoEntreLanusYQuilmes)

    }

    @Test
    fun DadoUnVectorHumanoEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoTerrestreConLaMenorCantidadDeMovimientos() {

        val cantidadDeNodosParaLlegarADestino = 2

        val cantidadDeNodosParaLLegarAAvellaneda = ubicacionService.costoDeCaminoMasCorto(vectorHumano.ubicacion!!.nombreUbicacion, "Avellaneda")
        assert(cantidadDeNodosParaLlegarADestino == cantidadDeNodosParaLLegarAAvellaneda)
    }

    @Test
    fun DadoUnVectorAnimalEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoTerrestreConLaMenorCantidadDeMovimientos() {
        val cantidadDeNodosParaLlegarADestino = 2

        val cantidadDeNodosParaLLegarAAvellaneda = ubicacionService.costoDeCaminoMasCorto(vectorAnimal.ubicacion!!.nombreUbicacion, "Avellaneda")
        assert(cantidadDeNodosParaLlegarADestino == cantidadDeNodosParaLLegarAAvellaneda)
    }

    @Test
    fun DadoUnVectorInsectoEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoTerrestreConLaMenorCantidadDeMovimientos() {
        val cantidadDeNodosParaLlegarADestino = 2

        val cantidadDeNodosParaLLegarAAvellaneda = ubicacionService.costoDeCaminoMasCorto(vectorInsecto.ubicacion!!.nombreUbicacion, "Avellaneda")
        assertTrue(cantidadDeNodosParaLlegarADestino == cantidadDeNodosParaLLegarAAvellaneda)

    }

    @Test(expected = UbicacionNoAlcanzableException::class)
    fun DadoUnVectorHumanoEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoAereoConLaMenorCantidadDeMovimientosYSeLevantaUnaExcepcionPorqueNoPuedeMoversePorElCamino() {
        ubicacionService.conectar("Monte Grande", "Capital Federal", TipoCamino.Aereo.name)
        ubicacionService.conectar("Capital Federal", "Banfield", TipoCamino.Aereo.name)
        ubicacionService.conectar("Banfield", "Yrigoyen", TipoCamino.Aereo.name)

        vectorHumano.ubicacion = ubicacionService.recuperarPorNombre("Monte Grande")
        vectorService.actualizar(vectorHumano)

        ubicacionService.moverMasCorto(vectorHumano.id!!, "Yrigoyen")
    }

    //capacidad de contagio segun movimientos

    @Test
    fun DadoUnVectorHumanoSePruebaQueLaCapacidadDeExpansionEsEsDeTresUbicaciones() {
        val cantidadDeMovimientos = 2
        val cantidadaDeUbicacionesDadoDosMovimientos = 3
        val cantiadaDeUbicacionesDondePuedeLlegarUnHumanoConDosMovimientos = ubicacionService.capacidadDeExpansion(vectorHumano.id!!, cantidadDeMovimientos)
        assert(cantidadaDeUbicacionesDadoDosMovimientos == cantiadaDeUbicacionesDondePuedeLlegarUnHumanoConDosMovimientos)

    }

    @Test
    fun DadoUnVectorAnimalSePruebaQueLaCapacidadDeExpansionEsEsDeTresUbicaciones() {
        val cantidadDeMovimientos = 2
        val cantidadaDeUbicacionesDadoDosMovimientos = 3
        val cantiadaDeUbicacionesDondePuedeLlegarUnHumanoConDosMovimientos = ubicacionService.capacidadDeExpansion(vectorAnimal.id!!, cantidadDeMovimientos)
        assert(cantidadaDeUbicacionesDadoDosMovimientos == cantiadaDeUbicacionesDondePuedeLlegarUnHumanoConDosMovimientos)
    }

    @Test
    fun DadoUnVectorInsectoSePruebaQueLaCapacidadDeExpansionEsEsDeTresUbicaciones() {
        val cantidadDeMovimientos = 2
        val cantidadaDeUbicacionesDadoDosMovimientos = 3
        val cantiadaDeUbicacionesDondePuedeLlegarUnHumanoConDosMovimientos = ubicacionService.capacidadDeExpansion(vectorInsecto.id!!, cantidadDeMovimientos)
        assert(cantidadaDeUbicacionesDadoDosMovimientos == cantiadaDeUbicacionesDondePuedeLlegarUnHumanoConDosMovimientos)
    }

    @After
    fun borrartodos() {
        dataServiceHibernate.eliminarTodo()
    }

}