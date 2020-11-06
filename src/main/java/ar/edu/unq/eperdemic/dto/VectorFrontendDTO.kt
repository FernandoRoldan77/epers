package ar.edu.unq.eperdemic.dto

import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector

class VectorFrontendDTO(val tipoDeVector : TipoDeVector,
                        val nombreDeUbicacionPresente: String) {

    enum class TipoDeVector {
        Humano, Insecto, Animal
    }

    fun aModelo() : Vector {
        val vectorToModel = Vector(
                when (this.tipoDeVector){
                    TipoDeVector.Humano -> Humano()
                    TipoDeVector.Insecto -> Insecto()
                    TipoDeVector.Animal -> Animal()
                }
        )
        vectorToModel.ubicacion = Ubicacion(nombreDeUbicacionPresente)
        return vectorToModel
    }
}