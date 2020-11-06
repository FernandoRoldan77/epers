package ar.edu.unq.eperdemic.modelo.entities.patogeno

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.exception.FueraDeRangoExcepcion
import org.bson.codecs.pojo.annotations.BsonProperty
import java.io.Serializable
import javax.persistence.*

@Entity
class Patogeno(var tipo: String) : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @OneToMany(mappedBy = "patogeno", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var especies: MutableList<Especie> = mutableListOf()

    private var letalidad: Int = 0
    var defensa: Int = 0

    override fun toString(): String {
        return tipo
    }

    fun setLetalidad(n: Int) {
        letalidad = n
    }

    fun crearEspecie(nombreEspecie: String, paisDeOrigen: String): Especie {
        val especieNew = Especie(this, nombreEspecie, paisDeOrigen)
        especies.add(especieNew)
        return especieNew
    }

    fun getLetalidad(): Int {
        return letalidad
    }

}

