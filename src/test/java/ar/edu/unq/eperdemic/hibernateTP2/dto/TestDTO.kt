package ar.edu.unq.eperdemic.hibernateTP2.dto

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import org.junit.Assert.assertEquals
import org.junit.Test

class TestDTO {

    @Test
    fun `crear dto vector to frontend`() {
        val vector = Vector(Humano())
        val ubicacion = Ubicacion("Moscu")
        vector.ubicacion = ubicacion

        val dto = VectorFrontendDTO(
                VectorFrontendDTO.TipoDeVector.valueOf(vector.getTipoBiologico()),
                vector.ubicacion!!.nombreUbicacion
        )
        assertEquals(dto.tipoDeVector.name, Humano().toString())
        assertEquals(dto.nombreDeUbicacionPresente, ubicacion.nombreUbicacion)
    }

    @Test
    fun `crear dto vector to backend`() {
        val vector = Vector(Humano())
        val ubicacion = Ubicacion("Moscu")
        vector.ubicacion = ubicacion

        val dto = VectorFrontendDTO(
                VectorFrontendDTO.TipoDeVector.valueOf(vector.getTipoBiologico()),
                vector.ubicacion!!.nombreUbicacion
        )

        val aModel = dto.aModelo()
        assertEquals(aModel.getTipoBiologico(), Humano().toString())
        assertEquals(aModel.ubicacion!!.nombreUbicacion, ubicacion.nombreUbicacion)
    }
}