package ar.edu.unq.eperdemic.modelo.entities.especie


import ar.edu.unq.eperdemic.modelo.entities.mutacion.Mutacion
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.enums.TipoDeVector
import org.bson.codecs.pojo.annotations.BsonDiscriminator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.io.Serializable
import javax.persistence.*
import kotlin.jvm.Transient

@Entity
class Especie(
        @BsonIgnore
        @ManyToOne(cascade = [CascadeType.ALL])
        var patogeno: Patogeno,
        var nombre: String,
        var paisDeOrigen: String,
        @Transient
        var esPandemia: Boolean = false) : Serializable {

    @BsonIgnore
    @Column(unique = false, nullable = false)
    var puntosDeADN: Int = 0
        get() {
            val humanosInfactados = vectores.filter { it.estaContagiado && it.getTipoBiologico() == "Humano" }.size
            return if (humanosInfactados >= 5) {
                calcularAdnCada5Personas(humanosInfactados) - mutaciones.sumBy { it.costoADN }
            } else 0
        }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @BsonIgnore
    @ManyToMany(mappedBy = "especies", cascade = [CascadeType.REMOVE], fetch = FetchType.EAGER)
    var vectores: MutableList<Vector> = mutableListOf()

    @BsonIgnore
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    var mutaciones: MutableList<Mutacion> = mutableListOf()

    val contagioContraAnimales: Int
        get() {
            return mutaciones.filter { it.infectaATipoVector.name == TipoDeVector.Animal.name }.sumBy { it.valorDeContagio }
        }

    val contagioContraHumanos: Int
        get() {
            return mutaciones.filter { it.infectaATipoVector.name == TipoDeVector.Humano.name }.sumBy { it.valorDeContagio }
        }

    val contagioContraInsectos: Int
        get() {
            return mutaciones.filter { it.infectaATipoVector.name == TipoDeVector.Insecto.name }.sumBy { it.valorDeContagio }
        }

    fun agregarMutacion(mutacionAgregar: Mutacion) {
        mutaciones.add(mutacionAgregar)
    }

    fun calcularAdnCada5Personas(cantidadPersonas: Int) = (cantidadPersonas / 5)

    fun cantidadDeContagioPorTipoBiologico(tipoBiologico: String): Int {
        return contagioContraHumanos + contagioContraAnimales + contagioContraInsectos
    }

    fun contieneMutacionesRequeridas(requeridas: List<Mutacion>): Boolean {
        return requeridas.all { mutaciones.any { m -> m.nombre == it.nombre } }
    }

}