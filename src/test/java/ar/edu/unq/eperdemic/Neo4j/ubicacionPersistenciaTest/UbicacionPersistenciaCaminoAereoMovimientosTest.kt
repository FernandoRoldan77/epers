package ar.edu.unq.eperdemic.Neo4j.ubicacionPersistenciaTest

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.*
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.modelo.exception.UbicacionNoAlcanzableException
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionPersistenciaCaminoAereoMovimientosTest : DataService {

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
    fun `puede llegar a una ubicacion lindante por camino aereo para vector animal`() {
        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        val ubicacionDestino = ubicacionService.recuperarPorNombre("Lanus")
        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Aereo.name)

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
    fun `puede llegar a una ubicacion lindante por camino aereo para vector insecto`() {

        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        val ubicacionDestino = ubicacionService.recuperarPorNombre("Lanus")
        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Aereo.name)

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
    @Test(expected = UbicacionNoAlcanzableException::class)
    fun `no puede llegar a una ubicacion lindante por camino aereo para vector humano`() {

        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        val ubicacionDestino = ubicacionService.recuperarPorNombre("Lanus")
        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Aereo.name)

        val vectorGlew = vectorService.recuperarTodosVectores().find {
            it.ubicacion!!.nombreUbicacion == "Glew" &&
                    it.getTipoBiologico() == Humano().toString() &&
                    it.estaContagiado
        }!!

        ubicacionOrigen.esUbicacionAlcanzable(
                ubicacionService.esUbicacionAlcanzable(
                        vectorGlew.ubicacion!!.nombreUbicacion,
                        ubicacionDestino.nombreUbicacion,
                        vectorGlew.obtenerCaminosPosibles()
                )
        )
    }
        @Test
        fun persistirCuandoUnVectorAnimalSeMuevaAUnaNuevAUbicacionConLaMenorCantidadDeMovimientosPosiblesPorUnCaminoAereo() {
            val vectorAnimalEnAdrogue = vectorService.recuperarTodosVectores().find {
                it.ubicacion!!.nombreUbicacion == "Glew" &&
                        it.getTipoBiologico() == Animal().toString() &&
                        it.estaContagiado
            }!!

            vectorAnimalEnAdrogue.moverAUbicacion(ubicacionService.recuperarPorNombre("Adrogue"))
            vectorService.actualizar(vectorAnimalEnAdrogue)

            ubicacionService.conectar("Adrogue", "Lomas de Zamora", TipoCamino.Aereo.name)
            ubicacionService.conectar("Adrogue", "Banfield", TipoCamino.Aereo.name)
            ubicacionService.conectar("Banfield", "Lanus", TipoCamino.Aereo.name)
            ubicacionService.conectar("Lanus", "Avellaneda", TipoCamino.Aereo.name)

            ubicacionService.moverMasCorto(vectorAnimalEnAdrogue.id!!, "Avellaneda")

            val vectorAnimalQueSeMovio = vectorService.recuperarVector(vectorAnimalEnAdrogue.id!!)

            Assert.assertEquals(vectorAnimalQueSeMovio.ubicacion!!.nombreUbicacion, "Avellaneda")

        }

    }