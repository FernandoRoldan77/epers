package ar.edu.unq.eperdemic.hibernateTP2.especie

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EspecieTest {

    private val ubicacionServiceImpl = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorServiceImpl = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoServiceImpl = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(),FeedMongoImplDAO())
    private val dataServiceHibernate = DataServiceHibernate(ubicacionServiceImpl, vectorServiceImpl, patogenoServiceImpl, especieServiceImpl, mutacionService)

    @Before
    fun preparar() {
        dataServiceHibernate.crearSetDeDatosIniciales()
    }

    @Test
    fun `crear nueva especie`() {
        val especieChicongunia = Especie(
                patogenoServiceImpl.recuperarPatogenoPorTipo("Hongo"),
                "Chicungunia",
                "Argentina"
        )
        assert(especieChicongunia.nombre == "Chicungunia")
    }

    @Test
    fun `crear nueva especie y persistirla`() {
        val especieChicongunia = Especie(
                patogenoServiceImpl.recuperarPatogenoPorTipo("Hongo"),
                "Chicungunia",
                "Argentina"
        )
        especieServiceImpl.crear(especieChicongunia)

        val especieChiconguniaPersistida = especieServiceImpl.recuperar(especieChicongunia.id!!)

        assertEquals(especieChicongunia.nombre, especieChiconguniaPersistida.nombre)
    }

    @Test
    fun `calcular adn para especie covid-19 sin mutaciones previas`() {
        val especieCovid19 = especieServiceImpl.recuperarTodos().find {
            it.nombre == "Covid-19"
        }!!

        val cantidadVectoresHumanosContagiados = especieCovid19.vectores.filter {
            it.estaContagiado && it.getTipoBiologico() == "Humano"
        }.size

        assertEquals(especieCovid19.puntosDeADN, cantidadVectoresHumanosContagiados / 5)
    }

    @Test
    fun `calcular adn para especie con humanos no infectados`() {
        val especieCovid19 = especieServiceImpl.recuperarTodos().find {
            it.nombre == "Fusarium"
        }!!

        val cantidadVectoresHumanosContagiados = especieCovid19.vectores.filter {
            it.estaContagiado && it.getTipoBiologico() == "Humano"
        }.size

        assert((cantidadVectoresHumanosContagiados / 5) == 0)
        assertEquals(especieCovid19.puntosDeADN, cantidadVectoresHumanosContagiados / 5)
    }

    @After
    fun eliminarTodaslasMutaciones() {
        dataServiceHibernate.eliminarTodo()
    }
}