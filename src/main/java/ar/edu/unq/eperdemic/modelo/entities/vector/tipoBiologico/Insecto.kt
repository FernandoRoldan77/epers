package ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico

import ar.edu.unq.eperdemic.modelo.enums.TipoCamino

class Insecto : TipoBiologico() {
    override fun toString(): String {
        return "Insecto"
    }
    override var organismoBiologico: MutableList<String> = super.organismoBiologico
    override fun agregarAgentesPropensosAContagio(): MutableList<String> = mutableListOf("Animal", "Humano")
    override val caminos = listOf(TipoCamino.Terrestre.name, TipoCamino.Aereo.name)
}