package ar.edu.unq.eperdemic.hibernateTP2.vector


import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy



class ProbabilidadDeContagioTest {
    private lateinit var vectorHumano: Vector
    lateinit var humano: Humano
    private lateinit var vectorAnimal: Vector
    lateinit var animal: Animal

    private lateinit var patogenoLetal: Patogeno
    private lateinit var patogenoNoLetal: Patogeno

    private lateinit var especieLetal: Especie
    private lateinit var especieNoLetal: Especie
    val probabilidadMock: ProbabilidadDeContagio = spy(ProbabilidadDeContagio::class.java)



    @Before
    fun preparar() {

        humano = Humano()
        vectorHumano = Vector(humano)
        animal = Animal()
        vectorAnimal = Vector(animal)

        patogenoLetal = Patogeno("Virus")
        patogenoNoLetal = Patogeno("Hongo")


        especieLetal = patogenoLetal.crearEspecie("Covid-19", "China")
        especieNoLetal = patogenoNoLetal.crearEspecie("Mycosphaerella", "Asia")


    }



    private fun setProbabilidadDeContagioMockConFactorContagio(vector: Vector, porcentajeDeContagioExitoso :Int, probabilidadDeContagio: Int){

        `when`(probabilidadMock.numeroAleatorio()).thenReturn(porcentajeDeContagioExitoso )
        `when`(probabilidadMock.probabilidadDeContagio(vector)).thenReturn(probabilidadDeContagio)
        vector.setProbabilidad(probabilidadMock)

    }

    @Test
    fun unVectorEsContagiadoPeroTieneGrandesPosibildadesParaEstarSano() {
        var probabilidadDeContagio=20
        var porcentajeDeContagioExitoso=100
        setProbabilidadDeContagioMockConFactorContagio(vectorHumano,porcentajeDeContagioExitoso,probabilidadDeContagio)


       vectorHumano.contraerEspecie(especieLetal)



        assert(porcentajeDeContagioExitoso>probabilidadDeContagio)
        assertFalse(vectorHumano.estaContagiado)
    }
    @Test
    fun unVectorEsContagiadoPeroTieneGrandesPosibildadesParaEstarEnfermo() {

        var probabilidadDeContagio=100
        var porcentajeDeContagioExitoso=20
        setProbabilidadDeContagioMockConFactorContagio(vectorAnimal,porcentajeDeContagioExitoso,probabilidadDeContagio)

        vectorAnimal.contraerEspecie(especieLetal)


        assert(porcentajeDeContagioExitoso<probabilidadDeContagio)
        assertTrue(vectorAnimal.estaContagiado)


    }


}