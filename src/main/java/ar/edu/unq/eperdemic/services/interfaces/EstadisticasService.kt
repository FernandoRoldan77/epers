package ar.edu.unq.eperdemic.services.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.ReporteDeContagios

interface EstadisticasService {
    fun especieLider(): Especie
    fun lideres(): List<Especie>
    fun reporteDeContagios(nombreUbicacion: String): ReporteDeContagios
}