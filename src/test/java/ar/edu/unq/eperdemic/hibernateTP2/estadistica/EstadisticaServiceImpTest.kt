package ar.edu.unq.eperdemic.hibernateTP2.estadistica

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.ReporteDeContagios
import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.exception.NoNameException
import ar.edu.unq.eperdemic.services.impl.*
import com.mongodb.internal.connection.tlschannel.util.Util.assertTrue
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class EstadisticaServiceImpTest {


    private val ubicacionServiceImpl = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorServiceImpl = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoServiceImpl = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())

    private val estadisticaServiceHibernate = EstadisticaServiceImpl(EstadisticaHibernateDAO(), AnalizadorDeDatosImp())

    private lateinit var especieTest: Especie
    private lateinit var especie: Especie
    private lateinit var patogeno: Patogeno
    private lateinit var patogeno2: Patogeno
    private lateinit var especie2: Especie
    private lateinit var ubicacion: Ubicacion



    private lateinit var vectorAnimal: Vector
    private lateinit var vectorHumano: Vector
    private lateinit var vectorHumano2: Vector
    private lateinit var vectorHumano3: Vector
    private lateinit var vectorHumano4: Vector
    private lateinit var vectorInsecto: Vector
    lateinit var humano: Humano
    private lateinit var animal: Animal
    private lateinit var insecto: Insecto
    private lateinit var vectores: List<Vector>
    private lateinit var especies: List<Especie>
    private lateinit var reporte: ReporteDeContagios

    private lateinit var analizador: AnalizadorDeDatosImp
    val probabilidadMock: ProbabilidadDeContagio = Mockito.spy(ProbabilidadDeContagio::class.java)

    @Before
    fun crearSetDeDatosIniciales() {

        ubicacion = ubicacionServiceImpl.crear("Quilmes")

        patogeno = Patogeno("Virus")

        patogeno2 = Patogeno("Hongo")


        patogenoServiceImpl.crearPatogeno(patogeno)
        patogenoServiceImpl.crearPatogeno(patogeno2)
        especie2 = Especie(patogeno2, "A713", "Alemania")
        especie = Especie(patogeno, "Covid", "China")
        especieServiceImpl.crear(especie)
        especieServiceImpl.crear(especie2)
        animal = Animal()
        humano = Humano()
        insecto = Insecto()
        vectorAnimal = Vector(animal)
        vectorAnimal.ubicacion=ubicacion
        vectorHumano = Vector(humano)
        vectorHumano.ubicacion=ubicacion
        vectorHumano2 = Vector(humano)
        vectorHumano2.ubicacion=ubicacion
        vectorHumano3 = Vector(humano)
        vectorHumano3.ubicacion=ubicacion
        vectorHumano4 = Vector(humano)
        vectorHumano4.ubicacion=ubicacion
        vectorInsecto = Vector(insecto)
        vectorInsecto.ubicacion=ubicacion
        vectorServiceImpl.crear(vectorHumano)
        vectorServiceImpl.crear(vectorHumano2)
        vectorServiceImpl.crear(vectorHumano3)
        vectorServiceImpl.crear(vectorHumano4)
        vectorServiceImpl.crear(vectorAnimal)
        vectorServiceImpl.crear(vectorInsecto)
        setDesignadorDeEstadoMock(vectorHumano)
        vectorServiceImpl.infectar(vectorHumano, especie)
        setDesignadorDeEstadoMock(vectorInsecto)

        vectorServiceImpl.infectar(vectorInsecto,especie2)

        vectores = listOf(vectorHumano, vectorAnimal, vectorInsecto, vectorHumano2, vectorHumano3, vectorHumano4)


        /////////////////////////////////////////


        especieTest = estadisticaServiceHibernate.especieLider()
        especies = estadisticaServiceHibernate.lideres()
        analizador = AnalizadorDeDatosImp()
        reporte = estadisticaServiceHibernate.reporteDeContagios("Quilmes")

    }
    private fun setDesignadorDeEstadoMock(aVector:Vector) {
        Mockito.`when`(probabilidadMock.calcularFactorContagio(aVector.getTipoBiologico(), aVector.especies)).thenReturn(100)

        aVector.setProbabilidad(probabilidadMock)
    }

    @Test
    fun cantidadDeVectoresPresentesEnQuilmes() {
        assert(vectores.size == reporte.vectoresPresentes)
    }

    @Test
    fun cantidadDeInfectadosEnQuilmes() {
        assertEquals(2 ,reporte.vectoresInfecatodos)
    }

    @Test
    fun hayDosPatogenosLideresEnELTop10() {
        setDesignadorDeEstadoMock(vectorHumano2)
        vectorServiceImpl.infectar(vectorHumano2, especie2)
        vectorServiceImpl.contagiar(vectorHumano2, vectores)
        var lideres=estadisticaServiceHibernate.lideres()
        assertTrue(lideres.size == 2)
    }

    @Test
    fun reporteDeLaEspecieMasContagiosa() {
        assert(especie.nombre == reporte.nombreDeEspecieMasInfecciosa)
    }

    @Test
    fun unHongoContagiaAMuchosHumanosYEsLaNuevaEspecieLider() {
        setDesignadorDeEstadoMock(vectorHumano2)
        vectorServiceImpl.infectar(vectorHumano2, especie2)
        vectorServiceImpl.contagiar(vectorHumano2, vectores)
        var especieLider=estadisticaServiceHibernate.especieLider()
        assert(especieLider.nombre == especie2.nombre)

    }

    @Test(expected = NoNameException::class)
    fun laUbicacionDebeExistirEnLaBaseDeDatos() {
        estadisticaServiceHibernate.reporteDeContagios("sadfawdfadsf")
    }


    @After
    fun eliminarTodo() {
        vectorServiceImpl.borrarTodasLasespecies()
        vectorServiceImpl.borrarTodos()
        ubicacionServiceImpl.eliminarTodasLasUbicaciones()
        especieServiceImpl.eliminarTodos()
        patogenoServiceImpl.eliminarTodosLosPatogenos()
    }

}