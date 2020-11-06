package ar.edu.unq.eperdemic.modelo.entities.vector

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.TipoBiologico
import ar.edu.unq.eperdemic.modelo.exception.NoNameException
import org.bson.codecs.pojo.annotations.BsonIgnore
import java.io.Serializable
import javax.persistence.*


@Entity
class Vector(tipoBiologico: TipoBiologico) : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Transient
    private var tipo: TipoBiologico = tipoBiologico
        get() {
            if (field == null) {
                setTipoBiologico(tipoBiologico!!)
            }
            return field
        }

    @Column(nullable = false, length = 500)
    private var tipoBiologico: String? = tipo.toString()

    @BsonIgnore
    @Transient
    private var probabilidadDeContagio = ProbabilidadDeContagio()
        get() {
            if (field == null) {
                field = ProbabilidadDeContagio()
            }
            return field
        }

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.EAGER)
    @JoinTable(name = "vector_especie", joinColumns = [JoinColumn(name = "vector_id")], inverseJoinColumns = [JoinColumn(name = "especie_id")])
    var especies: MutableList<Especie> = mutableListOf()

    @BsonIgnore
    @ManyToOne
    var ubicacion: Ubicacion? = null

    @Column(nullable = false, length = 500)
    var estaContagiado: Boolean = false

    fun evaluarEstadoDeSalud() = probabilidadDeContagio.evaluarProbabilidadDeContagioYCambiarEstadoDeSalud(this)

    fun puedeContagiar(vectorAContagiar: Vector): Boolean =
            tipo.puedeContagiarA(vectorAContagiar.getTipoBiologico())

    fun evaluarSiSePuedeProducirElContagio(vectorAContagiar: Vector):Boolean= estaInfectado() && this.puedeContagiar(vectorAContagiar)&& probabilidadDeContagio.esContagioExitoso(vectorAContagiar)

    fun contagiar(vectorAContagiar: Vector) {
        if (evaluarSiSePuedeProducirElContagio(vectorAContagiar)) {
            this.contagiarA(vectorAContagiar)
        }
    }

    private fun contagiarA(vectorAContagiar: Vector) {
        vectorAContagiar.sufrirContagio(this.especies)
        probabilidadDeContagio.evaluarProbabilidadDeContagioYCambiarEstadoDeSalud(vectorAContagiar)


    }
    private fun sufrirContagio(othersPatogenos: MutableList<Especie>) {
        especies.addAll(othersPatogenos)


    }


    fun estaInfectado(): Boolean {
        return if (this.estaContagiado.not()) {
            throw NoNameException("El vector no tiene ninguna enfermedad")
        } else {
            this.estaContagiado
        }
    }

    fun moverAUbicacion(ubicacionBase: Ubicacion) {
        this.ubicacion = ubicacionBase
    }

    fun contagiarVectores(vectores: MutableList<Vector>) {
        vectores.forEach { contagiar(it) }
    }

    fun contraerEspecie(especie: Especie) {
        if(probabilidadDeContagio.esContagioExitoso(this)) {
            especies.add(especie)
            evaluarEstadoDeSalud()
        }
    }

    fun getTipoBiologico(): String = tipoBiologico!!

    fun setProbabilidad(designadorDeEstados: ProbabilidadDeContagio) {
        this.probabilidadDeContagio = designadorDeEstados
    }

    fun setTipoBiologico(tipo: String) {
        when (tipo) {
            "Animal" -> this.tipo = Animal()
            "Insecto" -> this.tipo = Insecto()
            "Humano" -> this.tipo = Humano()
        }
    }


    fun contagiarVectoresPorVectorRandom(listaVectoresAContagiar: List<Vector>) {
        val vectoresContagiados = listaVectoresAContagiar.filter { it.estaContagiado }.toMutableList()
        val vectorNoContagiados = listaVectoresAContagiar.minus(vectoresContagiados).toMutableList()
        if (vectorNoContagiados.isNotEmpty() && vectoresContagiados.isNotEmpty()) {
            this.contagiarVectores(vectorNoContagiados)
        }
    }

    fun obtenerCaminosPosibles() = tipo.caminos
}