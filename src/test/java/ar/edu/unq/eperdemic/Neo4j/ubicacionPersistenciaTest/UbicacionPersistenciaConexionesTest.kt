package ar.edu.unq.eperdemic.Neo4j.ubicacionPersistenciaTest

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.*
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UbicacionPersistenciaConexionesTest : DataService {

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
        ubicacionService.crear("Lomas de Zamora")
        ubicacionService.crear("Marmol")
        ubicacionService.crear("Claypole")
        ubicacionService.crear("Burzaco")
        ubicacionService.crear("Bernal")
        ubicacionService.crear("Temperley")

        vectorHumano = Vector(humano)
        vectorHumano.ubicacion = ubicacionService.recuperarPorNombre("Lanus")
        vectorService.crear(vectorHumano)
    }
    @After
    override fun eliminarTodo() {
        dataServiceHibernate.eliminarTodo()
    }



    @Test
    fun `dado el nombre de una ubicacion retorna todas las ubicaciones conectadas a la ubicacion dada por cualquier tipo de camino`() {
        ubicacionService.conectar("Lanus", "Glew", TipoCamino.Terrestre.name)
        ubicacionService.conectar("Lanus", "Avellaneda", TipoCamino.Maritimo.name)
        ubicacionService.conectar("Lanus","Ezeiza",TipoCamino.Aereo.name)
        val ubicacionesConectadasALanuse = ubicacionService.conectados("Lanus")
        assertEquals(ubicacionesConectadasALanuse.map { it.nombreUbicacion }.sorted(), listOf("Glew", "Avellaneda","Ezeiza").sorted())
    }

    @Test
    fun `no hay ninguna ubicacion conectada por ningun camino a la ubicacion dada`() {
        val ubicacionesConectadasAMonteGrande = ubicacionService.conectados("Monte Grande")
        assert(ubicacionesConectadasAMonteGrande.isEmpty())
    }

    @Test
    fun `puede llegar a otra ubicacion lindante dada por medio de un camino cualquiera`() {
        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")

        assert(ubicacionService.conectados(ubicacionOrigen.nombreUbicacion).isEmpty())

        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Terrestre.name)
        assert(
                !ubicacionService.esUbicacionMuyLejana(
                        "Glew", "Lanus"
                )
        )


        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Maritimo.name)
        assert(!ubicacionService.esUbicacionMuyLejana(
                "Glew", "Lanus"
        ))

        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Maritimo.name)
        assert(
                !ubicacionService.esUbicacionMuyLejana(
                        "Glew", "Lanus"
                )
        )
    }

}