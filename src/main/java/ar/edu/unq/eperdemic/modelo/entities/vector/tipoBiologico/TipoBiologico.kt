package ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico


abstract class TipoBiologico {
    abstract val caminos: List<String>
    abstract override fun toString(): String
    open var organismoBiologico: MutableList<String> = agregarAgentesPropensosAContagio()
    open fun puedeContagiarA(tipo: String): Boolean = organismoBiologico.any { i -> i == tipo }
    abstract fun agregarAgentesPropensosAContagio(): MutableList<String>
}
