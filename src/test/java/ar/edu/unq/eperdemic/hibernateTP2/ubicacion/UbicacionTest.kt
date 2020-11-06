package ar.edu.unq.eperdemic.hibernateTP2.ubicacion

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UbicacionTest : DataService {

    private val ubicacionService = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorService = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoService = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieService = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(),FeedMongoImplDAO())
    private val dataServiceHibernate = DataServiceHibernate(ubicacionService, vectorService, patogenoService, especieService, mutacionService)

    @Before
    override fun crearSetDeDatosIniciales() {
        dataServiceHibernate.crearSetDeDatosIniciales()
    }


    // Folding TEST MODEL

    @Test
    fun `cambiar ubicacion a vector`() {
        // create model with constructor mock
        val vector = Vector(Humano())
        val ubicacionDummy = Ubicacion("Lanús")

        // cambio ubicacion
        vector.moverAUbicacion(ubicacionDummy)

        // verifico que la ubicacion se haya cambiado
        assertEquals(vector.ubicacion!!.nombreUbicacion, ubicacionDummy.nombreUbicacion)
    }

    // Folding TEST PERSIST

    @Test
    fun `cambiar de ubicacion al vector e intentar contagiar al resto de los vectores en nueva ubicacion`() {
        val ubicacionOrigen = ubicacionService.recuperarPorNombre("Glew")
        ubicacionService.conectar("Glew", "Lanus", TipoCamino.Terrestre.name)

        val vectoresToChange = ubicacionOrigen.vectores.find {
            it.estaContagiado && it.getTipoBiologico() == Humano().toString()
        }

        // persisto el cambio de ubicación
        ubicacionService.mover(vectoresToChange!!.id!!, "Lanus")

        // recupero objeto de la base
        val vectorBase = vectorService.recuperar(vectoresToChange.id!!)

        // compruebo si se cambio la ubicacion
        assertEquals("Lanus", vectorBase.ubicacion!!.nombreUbicacion)
    }

    @Test
    fun `tomar un vector aleatorio e intentar contagiar al resto en una ubicacion`() {
        val ubicacion = ubicacionService.recuperarTodasLasUbicaciones().find { it.nombreUbicacion == "Avellaneda" }

        // calculos cantidad de infectados para evaluar luego de expandir el germen
        val infectadosBeforeExpandir = ubicacion!!.vectores.filter { it.estaContagiado }.size

        ubicacionService.expandir(ubicacion.nombreUbicacion)

        val ubicacionBase = ubicacionService.recuperar(ubicacion.id!!)

        val infectadosAfterExpandir = ubicacionBase.vectores.filter { it.estaContagiado }.size

        assert(infectadosBeforeExpandir < infectadosAfterExpandir)
    }

    @Test
    fun `conectar dos ubicaciones`() {
        ubicacionService.conectar("Lanus", "Avellaneda", "Maritimo")
    }

    @After
    override fun eliminarTodo() {
        dataServiceHibernate.eliminarTodo()
    }

}