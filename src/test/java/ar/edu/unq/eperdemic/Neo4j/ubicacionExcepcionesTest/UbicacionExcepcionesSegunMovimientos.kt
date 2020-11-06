package ar.edu.unq.eperdemic.Neo4j.ubicacionExcepcionesTest

import ar.edu.unq.eperdemic.dao.hibernate.impl.PatogenoHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.UbicacionHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.VectorDAOImpl
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector.*
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.*
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector


import ar.edu.unq.eperdemic.modelo.exception.UbicacionMuyLejanaException
import ar.edu.unq.eperdemic.modelo.exception.UbicacionNoAlcanzableException
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import org.junit.After
import org.junit.Before
import org.junit.Test

class UbicacionExcepcionesTest {

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
        ubicacionService.crear("Palermo")
        ubicacionService.crear("Wilde")
//

        val ubicacionLanus = ubicacionService.recuperarPorNombre("Lanus")
        val ubicacionWilde = ubicacionService.recuperarPorNombre("Wilde")

        vectorHumano.ubicacion = ubicacionLanus
        vectorService.crear(vectorHumano)

        vectorInsecto.ubicacion = ubicacionWilde
        vectorService.crear(vectorInsecto)

    }


    //ubicacion  con excepciones por alejamiento

    //camino terrestre
    @Test(expected = UbicacionMuyLejanaException::class)
    fun CrearUnaUbicacionQueAlQuererMoverseConUnVectorHumanoAOtraUbicacionPorUnCaminoTerrestreNoPuedaPorEstarMuyAlejada() {
        ubicacionService.mover(vectorHumano.id!!, "Palermo")

    }

    @Test(expected = UbicacionMuyLejanaException::class)
    fun CrearUnaUbicacionQueAlQuererMoverseConUnVectorInsectoAOtraUbicacionPorUnCaminoTerrestreNoPuedaPorEstarMuyAlejada() {
        vectorInsecto.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorInsecto)
        ubicacionService.mover(vectorInsecto.id!!, "Palermo")
    }

    @Test(expected = UbicacionMuyLejanaException::class)
    fun CrearUnaUbicacionQueAlQuererMoverseConUnVectorAnimalAOtraUbicacionPeroNoPuedaPorEstarMuyAlejada() {
        vectorAnimal.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorAnimal)
        ubicacionService.mover(vectorAnimal.id!!, "Palermo")

    }
    @Test(expected = UbicacionMuyLejanaException::class)
    fun `no puede llegar a otra ubicacion lindante sin tener algun camino de por medio`() {
        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        val ubicacionDestino = ubicacionService.recuperarPorNombre("Lanus")
        ubicacionOrigen.esUbicacionMuyLejana(ubicacionService.esUbicacionMuyLejana(
                ubicacionOrigen.nombreUbicacion, ubicacionDestino.nombreUbicacion
        ), ubicacionDestino.nombreUbicacion)
    }

    //ubicacion con excepciones por un camino que no se puede atravesar

    @Test(expected = UbicacionNoAlcanzableException::class)
    fun CrearUnaUbicacionConUnVectorInsectoQueQuieraMoverseAOtraUbicacionPorUnCaminoMaritimoYNoPueda() {
        val tipoDeCaminoIngresado = "Maritimo"
        ubicacionService.conectar("Lanus", "Quilmes", tipoDeCaminoIngresado)
        vectorInsecto.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorInsecto)
        ubicacionService.mover(vectorInsecto.id!!, "Quilmes")
    }

    @Test(expected = UbicacionNoAlcanzableException::class)
    fun CrearUnaUbicacionQuePuedaMoverseConUnVectorHumanoAOtraUbicacionPorUnCaminoAereoYNoPueda() {
        val tipoDeCaminoIngresado = "Aereo"
        ubicacionService.conectar("Lanus", "Quilmes", tipoDeCaminoIngresado)
        ubicacionService.mover(vectorHumano.id!!, "Quilmes")
    }

    @After
    fun eliminarTodo() {
        vectorService.borrarTodos()
        ubicacionService.eliminarTodasLasUbicaciones()
    }


}