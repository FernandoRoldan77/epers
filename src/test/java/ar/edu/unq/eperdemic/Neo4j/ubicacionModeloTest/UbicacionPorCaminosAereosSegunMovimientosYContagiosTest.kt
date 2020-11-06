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

class UbicacionPorCaminosAereosSegunMovimientosYContagiosTest {
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

        ubicacionService.conectar("Lanus", "Glew", "Aereo")
        ubicacionService.conectar("Lanus", "Quilmes", "Aereo")
        ubicacionService.conectar("Quilmes", "Avellaneda", "Aereo")
        ubicacionService.conectar("Glew", "Avellaneda", "Aereo")

        val ubicacionLanus = ubicacionService.recuperarPorNombre("Lanus")

        vectorHumano.ubicacion = ubicacionLanus
        vectorService.crear(vectorHumano)

        vectorInsecto.ubicacion = ubicacionLanus
        vectorService.crear(vectorInsecto)

        vectorAnimal.ubicacion = ubicacionLanus
        vectorService.crear(vectorAnimal)
    }


    @Test
    fun CrearUnaUbicacionQuePuedaConectarseAOtraUbicacionPorUnCaminoAereo() {
        val tipoDeCaminoIngresado = "Aereo"
        val tipoDeCaminoEntreLanusYQuilmes = ubicacionService.caminoEntreDosUbicacionesLindantes("Lanus", "Quilmes")
        assert(tipoDeCaminoIngresado.equals(tipoDeCaminoEntreLanusYQuilmes))
    }

    @Test
    fun DadoUnVectorInsectoEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoAereoConLaMenorCantidadDeMovimientos() {
        val cantidadDeNodosParaLlegarADestino = 2
        ubicacionService.moverMasCorto(vectorInsecto.id!!, "Avellaneda")

        val cantidadDeNodosParaLLegarAAvellaneda = ubicacionService.costoDeCaminoMasCorto(vectorAnimal.ubicacion!!.nombreUbicacion, "Avellaneda")
        assert(cantidadDeNodosParaLlegarADestino == cantidadDeNodosParaLLegarAAvellaneda)
    }

    @Test
    fun DadoUnVectorAnimalEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoAereoConLaMenorCantidadDeMovimientos() {

        val cantidadDeNodosParaLlegarADestino = 2

        ubicacionService.moverMasCorto(vectorAnimal.id!!, "Avellaneda")
        val cantidadDeNodosParaLLegarAAvellaneda = ubicacionService.costoDeCaminoMasCorto(vectorInsecto.ubicacion!!.nombreUbicacion, "Avellaneda")
        assert(cantidadDeNodosParaLlegarADestino == cantidadDeNodosParaLLegarAAvellaneda)
    }

    @Test(expected = UbicacionNoAlcanzableException::class)
    fun dadoUnVectorHumanoEsteQuiereLlegarAUnaNuevaUbicacionPorUnCaminoAereoConLaMenorCantidadDeMovimientosYSeLevantaUnaExcepcionPorqueNoPuedeMoversePorElCamino() {
        ubicacionService.moverMasCorto(vectorHumano.id!!, "Avellaneda")

    }

    @Test
    fun dadaUnaUbicacionSeRetornanTodasLasUbicacionesConectadasPorUnCaminoAereo() {
        val nombreDeLaUbicacionIngresada = "Lanus"

        ubicacionService.conectar(nombreDeLaUbicacionIngresada, "Quilmes", TipoCamino.Aereo.name)
        ubicacionService.conectar(nombreDeLaUbicacionIngresada, "Avellaneda", TipoCamino.Aereo.name)
        ubicacionService.conectar(nombreDeLaUbicacionIngresada,"Glew",TipoCamino.Aereo.name)
        val cantidadDeCaminosTerrestresConectados = 3
        val cantidadDeCaminosDeLanusObtenidos =
                ubicacionService.cantidadaDeCaminosPorTipoDeCamino(
                        nombreDeLaUbicacionIngresada,
                        TipoCamino.Aereo.name
                )
        assert(cantidadDeCaminosTerrestresConectados == cantidadDeCaminosDeLanusObtenidos)
    }

    @Test
    fun dadoUnVectorInsectoSePruebaQueLaCapacidadDeExpansionEsEsDeTresUbicaciones() {
        val cantidadDeMovimientos = 2
        val cantidadaDeUbicacionesDadoDosMovimientos = 3
        val cantiadaDeUbicacionesDondePuedeLlegarUnHumanoConDosMovimientos = ubicacionService.capacidadDeExpansion(vectorInsecto.id!!, cantidadDeMovimientos)
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