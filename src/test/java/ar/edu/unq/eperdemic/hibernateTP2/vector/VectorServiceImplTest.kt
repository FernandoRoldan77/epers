package ar.edu.unq.eperdemic.hibernateTP2.vector

import ar.edu.unq.eperdemic.dao.hibernate.impl.EspecieHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.PatogenoHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.UbicacionHibernateDAO

import ar.edu.unq.eperdemic.dao.hibernate.impl.VectorDAOImpl
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto

import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.exception.NoNameException
import ar.edu.unq.eperdemic.services.impl.EspecieServiceImpl
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import org.junit.After


import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import kotlin.properties.Delegates

class VectorServiceImplTest {
    private lateinit var vectorServiceImpl:VectorServiceImpl
    private lateinit var ubicacionServiceImpl:UbicacionServiceImpl
    private lateinit var especieServiceImpl:EspecieServiceImpl
    private lateinit var patogenoHDAO: PatogenoHibernateDAO
    private lateinit var vectorDAOImpl: VectorDAOImpl
    private lateinit var patogenoServiceImpl: PatogenoServiceImpl
    private lateinit var vectorAnimal: Vector
    private lateinit var vectorHumano: Vector
    private lateinit var vectorInsecto:Vector
    lateinit var humano: Humano
    private lateinit var animal: Animal
    private lateinit var patogeno: Patogeno
    private lateinit var insecto:Insecto
    private lateinit var especie:Especie
    private lateinit var vectores:MutableList<Vector>
    private lateinit var vectores2:MutableList<Vector>
    private var idPatogeno by Delegates.notNull<Int>()
    val estadoMock: ProbabilidadDeContagio = Mockito.spy(ProbabilidadDeContagio::class.java)

    @Before
    fun preparar() {

        patogenoHDAO= PatogenoHibernateDAO()
        vectorDAOImpl= VectorDAOImpl()
         ubicacionServiceImpl = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
        vectorServiceImpl= VectorServiceImpl(vectorDAOImpl, FeedMongoImplDAO())
        patogenoServiceImpl= PatogenoServiceImpl(patogenoHDAO, FeedMongoImplDAO())
        especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())
        humano= Humano()
        animal= Animal()
        insecto= Insecto()
        vectorInsecto=Vector(insecto)
        vectorHumano = Vector(humano)
        vectorAnimal= Vector(animal)
        patogeno = Patogeno("Virus")

        patogeno.setLetalidad(100)


        vectorServiceImpl.crear(vectorAnimal)
        vectorServiceImpl.crear(vectorHumano)
        vectorServiceImpl.crear(vectorInsecto)

        vectores= mutableListOf(vectorInsecto,vectorHumano)
        vectores2= mutableListOf(vectorInsecto,vectorAnimal)
        idPatogeno=patogenoServiceImpl.crearPatogeno(patogeno)
        especie=patogenoServiceImpl.agregarEspecie(idPatogeno,"Covid-19","China")
    }
    private fun setDesignadorDeEstadoMock(aVector:Vector) {
        Mockito.`when`(estadoMock.calcularFactorContagio(aVector.getTipoBiologico(), aVector.especies)).thenReturn(100)
        aVector.setProbabilidad(estadoMock)
    }

    @Test
    fun contagiarTest(){
        setDesignadorDeEstadoMock(vectorAnimal)
        setDesignadorDeEstadoMock(vectorHumano)
        setDesignadorDeEstadoMock(vectorInsecto)
        vectorServiceImpl.infectar(vectorAnimal,especie)
        vectorServiceImpl.contagiar(vectorAnimal,vectores)
        Assert.assertTrue(vectorHumano.estaContagiado)
        Assert.assertTrue(vectorAnimal.estaContagiado)
        Assert.assertTrue(vectorInsecto.estaContagiado)
    }

    @Test(expected = NoNameException::class)
    fun unVectorQueNoestaContagiadoNoPuedeInfectar(){
        vectorServiceImpl.contagiar(vectorAnimal,vectores)
        Assert.assertTrue(vectorHumano.estaContagiado)
        Assert.assertTrue(vectorAnimal.estaContagiado)
        Assert.assertTrue(vectorInsecto.estaContagiado)
    }

    @Test
   fun especiesDelVector(){
        setDesignadorDeEstadoMock(vectorAnimal)
       vectorServiceImpl.infectar(vectorAnimal,especie)
        val enfermedades=vectorServiceImpl.enfermedades(vectorAnimal.id!!)
       Assert.assertTrue(especie.nombre == enfermedades.first().nombre)
    }

    @After
    fun eliminarTodo(){
        especieServiceImpl.eliminarTodos()
        patogenoServiceImpl.eliminarTodosLosPatogenos()
        vectorServiceImpl.borrarTodos()
    }
}