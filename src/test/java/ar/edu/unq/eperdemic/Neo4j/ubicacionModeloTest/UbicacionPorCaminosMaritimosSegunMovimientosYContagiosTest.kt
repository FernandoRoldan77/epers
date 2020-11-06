package ar.edu.unq.eperdemic.Neo4j.ubicacionModeloTest

import ar.edu.unq.eperdemic.dao.hibernate.impl.EspecieHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.PatogenoHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.UbicacionHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.VectorDAOImpl
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.*
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.modelo.exception.UbicacionNoAlcanzableException
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import org.junit.After
import org.junit.Before
import org.junit.Test

class UbicacionPorCaminosMaritimosSegunMovimientosYContagiosTest {
    private val humano = Humano()
    private val vectorHumano = Vector(humano)
    private val insecto = Insecto()
    private val vectorInsecto = Vector(insecto)
    private val animal = Animal()
    private val vectorAnimal = Vector(animal)
    private val ubicacionService = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorService = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())

    @Before
    fun crearSetDeDatosIniciales() {
        ubicacionService.crear("Lanus")
        ubicacionService.crear("Quilmes")
        ubicacionService.crear("Avellaneda")
        ubicacionService.crear("Glew")
        ubicacionService.conectar("Lanus", "Glew", TipoCamino.Maritimo.name)
        ubicacionService.conectar("Lanus", "Quilmes", TipoCamino.Maritimo.name)
        ubicacionService.conectar("Quilmes", "Avellaneda", TipoCamino.Maritimo.name)
        ubicacionService.conectar("Glew", "Avellaneda", TipoCamino.Maritimo.name)

        val ubicacionLanus = ubicacionService.recuperarPorNombre("Lanus")

        vectorHumano.ubicacion = ubicacionLanus
        vectorService.crear(vectorHumano)

        vectorInsecto.ubicacion = ubicacionLanus
        vectorService.crear(vectorInsecto)

        vectorAnimal.ubicacion = ubicacionLanus
        vectorService.crear(vectorAnimal)

    }

    @Test
    fun CrearUnaUbicacionQuePuedaConectarseAOtraUbicacionPorUnCaminoMaritimo() {
        ubicacionService.conectar("Lanus", "Quilmes", TipoCamino.Maritimo.name)
        val tipoDeCaminoEntreLanusYQuilmes = ubicacionService.caminoEntreDosUbicacionesLindantes("Lanus", "Quilmes")
        assert(TipoCamino.Maritimo.name == tipoDeCaminoEntreLanusYQuilmes)
    }
    @Test
    fun DadaUnaUbicacionSeRetornanTodasLasUbicacionesConectadasPorUnCaminoMaritimo() {
        val tipoDeCaminoIngresado = "Maritimo"
        val nombreDeLaUbicacionIngresada = "Lanus"
        val cantidadDeCaminosTerrestresConectados = 2
        val cantidadDeCaminosDeLanusObtenidos = ubicacionService.cantidadaDeCaminosPorTipoDeCamino(nombreDeLaUbicacionIngresada, tipoDeCaminoIngresado)

        assert(cantidadDeCaminosTerrestresConectados == cantidadDeCaminosDeLanusObtenidos)
    }

    @Test
    fun DadoUnVectorHumanoEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoMaritimoConLaMenorCantidadDeMovimientos() {
        val cantidadDeNodosParaLlegarADestino = 2
        ubicacionService.moverMasCorto(vectorHumano.id!!, "Avellaneda")
        val cantidadDeNodosParaLLegarAAvellaneda = ubicacionService.costoDeCaminoMasCorto(vectorHumano.ubicacion!!.nombreUbicacion, "Avellaneda")
        assert(cantidadDeNodosParaLlegarADestino == cantidadDeNodosParaLLegarAAvellaneda)
    }

    @Test
    fun DadoUnVectorAnimalEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoMaritimoConLaMenorCantidadDeMovimientos() {
        val cantidadDeNodosParaLlegarADestino = 2
        ubicacionService.moverMasCorto(vectorAnimal.id!!, "Avellaneda")
        val cantidadDeNodosParaLLegarAAvellaneda = ubicacionService.costoDeCaminoMasCorto(vectorAnimal.ubicacion!!.nombreUbicacion, "Avellaneda")
        assert(cantidadDeNodosParaLlegarADestino == cantidadDeNodosParaLLegarAAvellaneda)
    }



    @Test(expected = UbicacionNoAlcanzableException::class)
    fun DadoUnVectorInsectoEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoMaritimoConLaMenorCantidadDeMovimientosYSeLevantaUnaExcepcionPorqueNoPuedeMoversePorElCamino() {
        ubicacionService.moverMasCorto(vectorInsecto.id!!, "Avellaneda")
    }

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
    @After
    fun eliminarTodo() {
        vectorService.borrarTodos()
        ubicacionService.eliminarTodasLasUbicaciones()
    }
}