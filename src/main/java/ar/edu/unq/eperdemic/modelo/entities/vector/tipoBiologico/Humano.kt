package ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico

import ar.edu.unq.eperdemic.modelo.enums.TipoCamino

class Humano : TipoBiologico() {
    override fun toString(): String {
        return "Humano"
    }

    override var organismoBiologico: MutableList<String> = super.organismoBiologico
    override fun agregarAgentesPropensosAContagio(): MutableList<String> = mutableListOf("Insecto", "Humano")
    override val caminos = listOf(
            TipoCamino.Terrestre.name,
            TipoCamino.Maritimo.name
    )
}