package ar.edu.unq.eperdemic.Neo4j.ubicacionPersistenciaTest

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.*
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionPersistenciaCaminoTerrestreMovimientosTests : DataService {

    private lateinit var vectorHumano: Vector
    private lateinit var vectorAnimal: Vector
    private lateinit var vectorInsecto: Vector
    var humano: Humano = Humano()
    var animal: Animal = Animal()
    var insecto: Insecto = Insecto()
    private val ubicacionService = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorService = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoService = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieService = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(),FeedMongoImplDAO())
    private val dataServiceHibernate = DataServiceHibernate(ubicacionService, vectorService, patogenoService, especieService, mutacionService)

    @Before
    override fun crearSetDeDatosIniciales() {
        dataServiceHibernate.crearSetDeDatosIniciales()

        vectorHumano = Vector(humano)
        vectorHumano.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorHumano)

        vectorAnimal = Vector(animal)
        vectorAnimal.ubicacion = ubicacionService.crear("Adrogue")
        vectorService.crear(vectorAnimal)

        vectorInsecto = Vector(insecto)
        vectorInsecto.ubicacion = ubicacionService.crear("Varela")
        vectorService.crear(vectorInsecto)

        ubicacionService.crear("Ezeiza")
        ubicacionService.crear("Wilde")
        ubicacionService.crear("Bernal")

        vectorHumano = Vector(humano)
        vectorHumano.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorHumano)
    }

    @After
    override fun eliminarTodo() {
        dataServiceHibernate.eliminarTodo()
    }

    @Test
    fun persistirCuandoUnVectorHumanoSeMuevaAUnaNuevAUbicacionConLaMenorCantidadDeMovimientosPosiblesPorUnCaminoTerrestre() {
        val vectorHumanoConEnfermedades = vectorService.recuperarTodosVectores().filter {
            it.estaContagiado && it.ubicacion!!.nombreUbicacion == "Lanus"
        }.first()

        val nombreDeLaUbicacionInicialDelVector = vectorHumanoConEnfermedades.ubicacion!!.nombreUbicacion

        ubicacionService.conectar("Lanus", "Glew", "Terrestre")
        ubicacionService.conectar("Lanus", "Quilmes", "Terrestre")
        ubicacionService.conectar("Quilmes", "Avellaneda", "Terrestre")
        ubicacionService.conectar("Glew", "Avellaneda", "Terrestre")
        ubicacionService.moverMasCorto(vectorHumanoConEnfermedades.id!!, "Avellaneda")
        val nombreDeLaUbicacionFinalDelVector = vectorService.recuperarVector(vectorHumanoConEnfermedades.id!!).ubicacion!!.nombreUbicacion

        Assert.assertFalse(nombreDeLaUbicacionInicialDelVector == nombreDeLaUbicacionFinalDelVector)
    }

    @Test
    fun persistirCuandoUnVectorAnimalSeMuevaAUnaNuevAUbicacionConLaMenorCantidadDeMovimientosPosiblesPorUnCaminoTerrestre() {
        val vectorAnimalEnAdrogue = vectorService.recuperarTodosVectores().find {
            it.ubicacion!!.nombreUbicacion == "Lanus" &&
                    it.getTipoBiologico() == Animal().toString() &&
                    it.estaContagiado
        }!!

        vectorAnimalEnAdrogue.moverAUbicacion(ubicacionService.recuperarPorNombre("Adrogue"))
        vectorService.actualizar(vectorAnimalEnAdrogue)

        ubicacionService.conectar("Adrogue", "Quilmes", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Adrogue", "Temperley", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Quilmes", "Avellaneda", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Temperley", "Avellaneda", TipoCamino.Terrestre.name)
        ubicacionService.moverMasCorto(vectorAnimalEnAdrogue.id!!, "Avellaneda")

        val vectorAnimalQueSeMovio = vectorService.recuperarVector(vectorAnimalEnAdrogue.id!!)
        val vectorAnimalEnAvellaneda = vectorAnimalQueSeMovio.ubicacion!!.nombreUbicacion

        Assert.assertNotEquals(vectorAnimalEnAdrogue, vectorAnimalEnAvellaneda)
    }

    @Test
    fun persistirCuandoUnVectorInsectoSeMuevaAUnaNuevAUbicacionConLaMenorCantidadDeMovimientosPosiblesPorUnCaminoTerrestre() {
        val vectorInsectoEnVarela = vectorService.recuperarTodosVectores().find {
            it.ubicacion!!.nombreUbicacion == "Lanus" &&
                    it.getTipoBiologico() == Insecto().toString() &&
                    it.estaContagiado
        }!!

        vectorInsectoEnVarela.moverAUbicacion(ubicacionService.recuperarPorNombre("Varela"))
        vectorService.actualizar(vectorInsectoEnVarela)

        ubicacionService.conectar("Varela", "Berazategui", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Varela", "Bernal", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Berazategui", "Wilde", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Bernal", "Wilde", TipoCamino.Terrestre.name)
        ubicacionService.moverMasCorto(vectorInsectoEnVarela.id!!, "Wilde")

        val vectorInsectoQueSeMovio = vectorService.recuperarVector(vectorInsectoEnVarela.id!!)

        Assert.assertEquals(vectorInsectoQueSeMovio.ubicacion!!.nombreUbicacion, "Wilde")
    }
    @Test
    fun `puede llegar a una ubicacion lindante por camino terrestre para vector humano`() {
        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        val ubicacionDestino = ubicacionService.recuperarPorNombre("Lanus")
        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Terrestre.name)

        val vectorGlew = vectorService.recuperarTodosVectores().find {
            it.ubicacion!!.nombreUbicacion == "Glew" &&
                    it.getTipoBiologico() == Humano().toString() &&
                    it.estaContagiado
        }!!

        val esUbicacionAlcanzable = ubicacionOrigen.esUbicacionAlcanzable(
                ubicacionService.esUbicacionAlcanzable(
                        vectorGlew.ubicacion!!.nombreUbicacion,
                        ubicacionDestino.nombreUbicacion,
                        vectorGlew.obtenerCaminosPosibles()
                )
        )

        assert(esUbicacionAlcanzable)
    }
    @Test
    fun `puede llegar a una ubicacion lindante por camino terrestre para vector animal`() {
        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        val ubicacionDestino = ubicacionService.recuperarPorNombre("Lanus")
        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Terrestre.name)

        val vectorGlew = vectorService.recuperarTodosVectores().find {
            it.ubicacion!!.nombreUbicacion == "Glew" &&
                    it.getTipoBiologico() == Animal().toString() &&
                    it.estaContagiado
        }!!

        val esUbicacionAlcanzable = ubicacionOrigen.esUbicacionAlcanzable(
                ubicacionService.esUbicacionAlcanzable(
                        vectorGlew.ubicacion!!.nombreUbicacion,
                        ubicacionDestino.nombreUbicacion,
                        vectorGlew.obtenerCaminosPosibles()
                )
        )

        assert(esUbicacionAlcanzable)
    }
    @Test
    fun `puede llegar a una ubicacion lindante por camino terrestre para vector insecto`() {
        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        val ubicacionDestino = ubicacionService.recuperarPorNombre("Lanus")
        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Terrestre.name)

        val vectorGlew = vectorService.recuperarTodosVectores().find {
            it.ubicacion!!.nombreUbicacion == "Glew" &&
                    it.getTipoBiologico() == Insecto().toString() &&
                    it.estaContagiado
        }!!

        val esUbicacionAlcanzable = ubicacionOrigen.esUbicacionAlcanzable(
                ubicacionService.esUbicacionAlcanzable(
                        vectorGlew.ubicacion!!.nombreUbicacion,
                        ubicacionDestino.nombreUbicacion,
                        vectorGlew.obtenerCaminosPosibles()
                )
        )

        assert(esUbicacionAlcanzable)
    }
}