package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.AnalizadorDeDatos
import ar.edu.unq.eperdemic.dao.hibernate.interfaces.EstadisticaDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.ReporteDeContagios
import ar.edu.unq.eperdemic.services.interfaces.EstadisticasService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class EstadisticaServiceImpl(private val estadisticaH: EstadisticaDAO, private val analizadorDeDatos: AnalizadorDeDatos) : EstadisticasService {
    override fun especieLider(): Especie = TransactionRunner.runTrx { estadisticaH.especieLider() }
    override fun lideres(): List<Especie> = TransactionRunner.runTrx { estadisticaH.lideres() }
    override fun reporteDeContagios(nombreUbicacion: String): ReporteDeContagios = TransactionRunner.runTrx { analizadorDeDatos.reporteDeContagios(nombreUbicacion) }

}