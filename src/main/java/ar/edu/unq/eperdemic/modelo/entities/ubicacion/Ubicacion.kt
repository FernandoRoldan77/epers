package ar.edu.unq.eperdemic.modelo.entities.ubicacion

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.TipoBiologico
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.exception.UbicacionMuyLejanaException
import ar.edu.unq.eperdemic.modelo.exception.UbicacionNoAlcanzableException
import org.bson.codecs.pojo.annotations.BsonIgnore
import java.io.Serializable
import javax.persistence.*

@Entity
class Ubicacion(@Column(unique = true, nullable = false, length = 500) var nombreUbicacion: String = "") : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @BsonIgnore
    @OneToMany(mappedBy = "ubicacion", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var vectores: MutableList<Vector> = mutableListOf()

    fun esUbicacionMuyLejana(esUbicacionMuyLejana: Boolean, UbicacionDestino: String) {
        if (esUbicacionMuyLejana) {
            throw UbicacionMuyLejanaException(
                    "El vector no puede llegar desde la ubicación ${nombreUbicacion} " +
                            "a la nueva ubicacion ${UbicacionDestino} por medio de un camino."
            )
        }
    }

    fun esUbicacionAlcanzable(esAlcanzable: Boolean): Boolean {
        if (!esAlcanzable) {
            throw UbicacionNoAlcanzableException("Se intento mover a un vector a través de " +
                    "un tipo de camino que no puede atravesar.")
        }
        return esAlcanzable
    }





}