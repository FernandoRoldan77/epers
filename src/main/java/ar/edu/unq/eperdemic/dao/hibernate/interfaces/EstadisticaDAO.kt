package ar.edu.unq.eperdemic.dao.hibernate.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie

interface EstadisticaDAO {
    fun especieLider(): Especie
    fun lideres(): List<Especie>
}