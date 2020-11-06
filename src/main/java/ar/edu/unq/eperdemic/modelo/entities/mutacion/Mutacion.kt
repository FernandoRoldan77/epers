package ar.edu.unq.eperdemic.modelo.entities.mutacion

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.enums.TipoDeVector
import ar.edu.unq.eperdemic.modelo.exception.ADNInsuficienteException
import ar.edu.unq.eperdemic.modelo.exception.RequerimientoException
import org.bson.codecs.pojo.annotations.BsonDiscriminator
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.io.Serializable
import javax.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TipoDeMutacion", discriminatorType = DiscriminatorType.STRING)
abstract class Mutacion(@Column(unique = true, nullable = false)
                        val nombre: String,
                        val infectaATipoVector: TipoDeVector,
                        val valorDeContagio: Int
) : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(unique = false, nullable = false, length = 500)
    var costoADN: Int = 0

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "parent", referencedColumnName = "id")
    var parent: Mutacion? = null

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @Column(nullable = true)
    @Fetch(FetchMode.SUBSELECT)
    var mutacionesRequeridas: MutableList<Mutacion> = mutableListOf()

    fun agregarRequerimiento(requerimientoMutacion: Mutacion) {
        mutacionesRequeridas.add(requerimientoMutacion)
    }

    fun mutar(especieAMutar: Especie) {
        if (mutacionesRequeridas.size > 0) {
            validarMutacionesRequeridas(especieAMutar)
        }
        aumentarFactorContagioSiPuede(especieAMutar)
    }

    private fun validarMutacionesRequeridas(especieAMutar: Especie) {
        if (!especieAMutar.contieneMutacionesRequeridas(mutacionesRequeridas)) {
            throw RequerimientoException("La especie ${especieAMutar.nombre} no tiene la mutacion que se requiere.")
        }
    }

    private fun aumentarFactorContagioSiPuede(especieAMutar: Especie) {
        if (costoADN > especieAMutar.puntosDeADN) {
            throw ADNInsuficienteException("La especie ${especieAMutar.nombre} no tiene suficiente ADN para mutar.")
        }
        agregarMutacionYAumentarFactorContagio(especieAMutar)
    }

    fun agregarMutacionYAumentarFactorContagio(especieAMutar: Especie) {
        especieAMutar.agregarMutacion(this)
    }

    abstract fun identificadoresMutaciones(): List<String>

}