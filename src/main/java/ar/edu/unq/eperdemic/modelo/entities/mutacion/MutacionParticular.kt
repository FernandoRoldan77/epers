package ar.edu.unq.eperdemic.modelo.entities.mutacion

import ar.edu.unq.eperdemic.modelo.enums.TipoDeVector
import javax.persistence.Entity

@Entity
class MutacionParticular(nombre: String,
                         infectaATipoVector: TipoDeVector,
                         valorDeContagio: Int) : Mutacion(nombre, infectaATipoVector, valorDeContagio) {

    override fun identificadoresMutaciones(): List<String> {
        return listOf(nombre)
    }
}
