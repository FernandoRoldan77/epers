package ar.edu.unq.eperdemic.hibernateTP2.vector

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.exception.NoNameException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VectorModelTest {


    private lateinit var vectorAnimal: Vector
    private lateinit var vectorAnimal2: Vector
    private lateinit var vectorHumano: Vector
    private lateinit var vectorHumano2: Vector
    private lateinit var vectorInsecto: Vector
    private lateinit var vectorInsecto2: Vector
    lateinit var humano: Humano
    private lateinit var animal: Animal
    private lateinit var patogenoLetal: Patogeno
    private lateinit var patogenoNoLetal: Patogeno
    private lateinit var insecto: Insecto
    private lateinit var especieLetal: Especie
    private lateinit var especieLetal2: Especie
    private lateinit var especieNoLetal: Especie
    private lateinit var vectores: MutableList<Vector>
    private lateinit var vectores2: MutableList<Vector>
    val estadoMock: ProbabilidadDeContagio = spy(ProbabilidadDeContagio::class.java)


    @Before
    fun preparar() {

        humano = Humano()
        animal = Animal()
        insecto = Insecto()
        vectorInsecto = Vector(insecto)
        vectorInsecto2 = Vector(insecto)
        vectorHumano = Vector(humano)
        vectorHumano2 = Vector(humano)
        vectorAnimal = Vector(animal)
        vectorAnimal2 = Vector(animal)
        patogenoLetal = Patogeno("Virus")
        patogenoNoLetal = Patogeno("Hongo")

        patogenoLetal.setLetalidad(100)
        patogenoNoLetal.setLetalidad(1)
        especieLetal = patogenoLetal.crearEspecie("Covid-19", "China")
        especieLetal2 = patogenoLetal.crearEspecie("Sars", "China")
        especieNoLetal = patogenoNoLetal.crearEspecie("Mycosphaerella", "Asia")



        vectores = mutableListOf(vectorInsecto, vectorHumano)
        vectores2 = mutableListOf(vectorInsecto, vectorAnimal)


    }

    private fun setDesignadorDeEstadoMock(aVector:Vector) {
        `when`(estadoMock.calcularFactorContagio(aVector.getTipoBiologico(), aVector.especies)).thenReturn(100)
        aVector.setProbabilidad(estadoMock)
    }

    @Test
    fun UnVectorPuedeContagiarAOtroConUnaEspecieNoLetalYNoProducirContagio() {
        vectorHumano.contraerEspecie(especieNoLetal)
        Assert.assertFalse(vectorHumano.estaContagiado)
    }



    @Test
    fun vectorHumanoPuedeContagiarAOtroHumano() {
        setDesignadorDeEstadoMock(vectorHumano)
        setDesignadorDeEstadoMock(vectorHumano2)
        vectorHumano.contraerEspecie(especieLetal)
        vectorHumano.contagiar(vectorHumano2)
        Assert.assertTrue(vectorHumano2.estaContagiado)
    }

    @Test
    fun vectorHumanoPuedeContagiarAUnInsecto() {

        setDesignadorDeEstadoMock(vectorHumano)
        setDesignadorDeEstadoMock(vectorInsecto)
        vectorHumano.contraerEspecie(especieLetal)
        vectorHumano.contagiar(vectorInsecto)
        Assert.assertTrue(vectorInsecto.estaContagiado)
    }

    @Test
    fun vectorAnimalPuedeContagiarAUnInsecto() {

        setDesignadorDeEstadoMock(vectorAnimal)
        setDesignadorDeEstadoMock(vectorInsecto)

        vectorAnimal.contraerEspecie(especieLetal)
        vectorAnimal.contagiar(vectorInsecto)
        Assert.assertTrue(vectorInsecto.estaContagiado)
    }


    @Test
    fun vectorHumanoNoPuedeInfectarAVectorAnimal() {
        setDesignadorDeEstadoMock(vectorHumano)
        vectorHumano.contraerEspecie(especieLetal)
        vectorHumano.contagiar(vectorAnimal)
        Assert.assertFalse(vectorAnimal.estaContagiado)
        
    }

    @Test
    fun vectoAnimalSiPuedeInfectarAHumano() {
        setDesignadorDeEstadoMock(vectorAnimal)
        setDesignadorDeEstadoMock(vectorHumano)
        vectorAnimal.contraerEspecie(especieLetal)
        vectorAnimal.contagiar(vectorHumano)
        Assert.assertTrue(vectorHumano.estaContagiado)
    }

    @Test
    fun vectorInsectoPuedeContagiarAUnHumano() {
        setDesignadorDeEstadoMock(vectorInsecto)
        setDesignadorDeEstadoMock(vectorHumano)
        vectorInsecto.contraerEspecie(especieLetal)
        vectorInsecto.contagiar(vectorHumano)
        Assert.assertTrue(vectorHumano.estaContagiado)
    }

    @Test
    fun vectorInsectoPuedeContagiarAUnAnimal() {
        setDesignadorDeEstadoMock(vectorInsecto)
        setDesignadorDeEstadoMock(vectorAnimal)
        vectorInsecto.contraerEspecie(especieLetal)
        vectorInsecto.contagiar(vectorAnimal)
        Assert.assertTrue(vectorAnimal.estaContagiado)
    }

    @Test
    fun losInsectosNoSeContagianEntreSi() {
        setDesignadorDeEstadoMock(vectorInsecto)
        vectorInsecto.contraerEspecie(especieLetal)
        vectorInsecto.contagiar(vectorInsecto2)
        Assert.assertFalse(vectorInsecto2.estaContagiado)
    }


    @Test
    fun losAnimalesNoSeContagianEntreSi() {
        setDesignadorDeEstadoMock(vectorAnimal)
        vectorAnimal.contraerEspecie(especieLetal)
        vectorAnimal.contagiar(vectorAnimal2)
        Assert.assertFalse(vectorAnimal2.estaContagiado)
    }

    @Test
    fun `un vector animal puede infectarse con una especie y llegar a estar contagiado`() {
        setDesignadorDeEstadoMock(vectorAnimal)
        vectorAnimal.contraerEspecie(especieLetal)
        vectorAnimal.contraerEspecie(especieLetal)
        Assert.assertTrue(vectorAnimal.estaContagiado)
    }

    @Test
    fun vectorHumanoNoEstaContagiado() {
        Assert.assertFalse(vectorHumano.estaContagiado)
    }

    @Test
    fun `Un vector animal infectado puedo contagiar la especie con la que esta infectado a otro vector`() {
       setDesignadorDeEstadoMock(vectorAnimal)
        setDesignadorDeEstadoMock(vectorHumano)
        vectorAnimal.contraerEspecie(especieLetal)
        vectorAnimal.contagiar(vectorHumano)
        Assert.assertTrue(vectorHumano.estaContagiado)
    }

    @Test(expected = NoNameException::class)
    fun unVectorNoPuedeContagiarSiNoEstaInfectado() {
        vectorAnimal.contagiar(vectorHumano)
    }

    @Test
    fun `vector humano contagiado por varios especies`() {
        setDesignadorDeEstadoMock(vectorHumano)
        setDesignadorDeEstadoMock(vectorInsecto)
        vectorInsecto.contraerEspecie(especieLetal)
        vectorInsecto.contraerEspecie(especieLetal2)
        vectorInsecto.contraerEspecie(especieNoLetal)
        vectorInsecto.contagiar(vectorHumano)

        Assert.assertTrue(vectorHumano.estaContagiado)
        Assert.assertTrue(vectorInsecto.especies.size == 3)

    }


}