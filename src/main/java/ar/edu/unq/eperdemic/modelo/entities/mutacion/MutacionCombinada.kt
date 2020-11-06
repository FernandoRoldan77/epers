package ar.edu.unq.eperdemic.modelo.entities.mutacion

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.enums.TipoDeVector
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
class MutacionCombinada(nombre: String, infectaATipoVector: TipoDeVector, valorDeContagio: Int) : Mutacion(nombre, infectaATipoVector, valorDeContagio) {

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var children: MutableList<Mutacion> = mutableListOf()

    override fun identificadoresMutaciones(): List<String> {
        val nombresAcumulados = mutableListOf(nombre)
        for (child in this.children) {
            child.identificadoresMutaciones().forEach {
                nombresAcumulados.add(it)
            }
        }
        return nombresAcumulados
    }

    fun agregarMutacion(mutacion: Mutacion) {
        children.add(mutacion)
    }

    fun sacarMutacion(mutacion: Mutacion) {
        children.remove(mutacion)
    }

}


