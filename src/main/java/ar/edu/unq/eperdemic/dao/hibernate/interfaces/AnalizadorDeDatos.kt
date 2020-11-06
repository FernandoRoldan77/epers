package ar.edu.unq.eperdemic.dao.hibernate.interfaces

import ar.edu.unq.eperdemic.modelo.entities.ReporteDeContagios

interface AnalizadorDeDatos {
    fun cantidadEnUnaUbicacion(nombre: String): Int
    fun cantidadInfectadosEnUnaUbicacion(nombre: String): Int
    fun especieMasLetalEnUbicacion(nombre: String): String
    fun reporteDeContagios(nombre: String): ReporteDeContagios
}