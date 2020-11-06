package ar.edu.unq.eperdemic.hibernateTP2.patogeno

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.services.interfaces.EspecieService
import ar.edu.unq.eperdemic.services.interfaces.PatogenoService
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.properties.Delegates

class PatogenoServiceTest {

    private lateinit var data: HibernateDataDAO
    private lateinit var serviceImp: PatogenoService
    private lateinit var especieImp: EspecieService
    private lateinit var patogenoHDAO: PatogenoHibernateDAO
    private lateinit var patogeno: Patogeno
    private var idPatogeno by Delegates.notNull<Int>()

    private val ubicacionServiceImpl = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorServiceImpl = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoServiceImpl = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(),FeedMongoImplDAO())
    private val dataServiceHibernate = DataServiceHibernate(ubicacionServiceImpl, vectorServiceImpl, patogenoServiceImpl, especieServiceImpl, mutacionService)


    @Before
    fun preparar() {
        data = HibernateDataDAO()
        patogenoHDAO = PatogenoHibernateDAO()
        patogeno = Patogeno("Bacterias")
        especieImp = EspecieServiceImpl(EspecieHibernateDAO())
        patogeno = Patogeno("Covid19")
        serviceImp = PatogenoServiceImpl(patogenoHDAO, FeedMongoImplDAO())
        idPatogeno = serviceImp.crearPatogeno(patogeno)
        dataServiceHibernate.crearSetDeDatosIniciales()
    }

    @Test
    fun testCrearPatogeno() {
        Assert.assertEquals(patogeno.id, idPatogeno)
    }

    @Test
    fun recuperarPorTipo() {
        Assert.assertEquals(patogeno.tipo, serviceImp.recuperarPatogenoPorTipo("Covid19").tipo)
    }

    @Test
    fun recuperarPorId() {
        val patogenoRecuperado = serviceImp.recuperarPatogenoId(idPatogeno).id
        Assert.assertEquals(patogeno.id!!, patogenoRecuperado)
    }

    @Test
    fun recuperarTodosLosPatogenos() {
        Assert.assertEquals(dataServiceHibernate.patogenos.size + 1, serviceImp.recuperarTodosLosPatogenos().size)
    }

    @Test
    fun `recuperar todos los patogenos y comprobar order by`() {
        val lista = serviceImp.recuperarTodosLosPatogenos().map { it.tipo }
        val patogenosAsc = serviceImp.recuperarTodosLosPatogenos().sortedBy { it.tipo }.map { it.tipo }
        Assert.assertEquals(lista, patogenosAsc)
    }

    @Test
    fun agregarEspecie() {
        val especie = serviceImp.agregarEspecie(idPatogeno, "Covid", "China")
        patogeno = serviceImp.recuperarPatogenoId(idPatogeno)
        Assert.assertEquals(especie.patogeno.id, idPatogeno)
    }

    @Test
    fun cantidadDeInfectadosPorEspecie() {
        val especies = serviceImp.recuperarTodosLosPatogenos().flatMap { it.especies }.toMutableList()
        val especiesConVectoresContagiados = especies.filter { it.vectores.any { v -> v.estaContagiado } }
        val especieRandom = especiesConVectoresContagiados.random()
        val cantidadInfectados = serviceImp.cantidadDeInfectados(especieRandom.id!!)
        Assert.assertEquals(especieRandom.vectores.count { it.estaContagiado }, cantidadInfectados)
    }

    @Test
    fun esPandemia() {
        val especies = serviceImp.recuperarTodosLosPatogenos().flatMap { it.especies }.toMutableList()
        val especiesEnMasUbicaciones = especies.filter { it -> it.vectores.groupBy { it.ubicacion }.count() > 3 }
        val randomEspecie = especiesEnMasUbicaciones.random()
        assert(serviceImp.esPandemia(randomEspecie.id!!))
    }


    @After
    fun eliminarPatogenosTodos() {
        dataServiceHibernate.eliminarTodo()
    }
}