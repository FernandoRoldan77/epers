package ar.edu.unq.eperdemic.hibernateTP2.vector

import ar.edu.unq.eperdemic.dao.hibernate.impl.PatogenoHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.UbicacionHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.VectorDAOImpl
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.UbicacionDAO
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.exception.NoNameException
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.services.interfaces.VectorService
import com.mongodb.internal.connection.tlschannel.util.Util.assertTrue
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.After


import org.junit.Before
import org.junit.Test

class VectorServiceImplCRUDTest {

    private lateinit var vectorHumano: Vector
    lateinit var humano: Humano
    private lateinit var vectorDao: VectorDAOImpl
    private lateinit var vectorServiceImpl: VectorService
    private lateinit var ubicacionService:  UbicacionServiceImpl
    @Before
    fun preparar() {
        vectorDao = VectorDAOImpl()
        ubicacionService= UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
        vectorServiceImpl = VectorServiceImpl(vectorDao, FeedMongoImplDAO())
        humano = Humano()
        vectorHumano = Vector(humano)
        vectorServiceImpl.crear(vectorHumano)

    }

    @Test
    fun recuperarVector() {

        val vectorRecuperado = vectorServiceImpl.recuperarVector((vectorHumano.id!!))
        assertThat(vectorRecuperado).isEqualToComparingFieldByFieldRecursively(vectorHumano)

    }

    @Test(expected = NoNameException::class)
    fun noSePuedeRecuperarUnVectorConIdQueNoEstePersistido() {
        vectorServiceImpl.recuperarVector(9999)
    }


    @Test
    fun recuperarVectorPorTipo() {
        val vectorAnimal = vectorServiceImpl.recuperarVectorPorTipoBIologico("Humano")
       assertTrue("Humano" == vectorAnimal.getTipoBiologico())
    }

    @After
    fun eleiminarTodosLosVectores() {
        vectorServiceImpl.borrarTodos()
    }

}