package ar.edu.unq.eperdemic.services.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno

interface PatogenoService {
    fun agregarEspecie(id: Int, nombreEspecie: String, paisDeOrigen: String): Especie
    fun cantidadDeInfectados(especieId: Int): Int
    fun esPandemia(especieId: Int): Boolean
    fun crearPatogeno(patogeno: Patogeno): Int
    fun actualizarPatogeno(patogeno: Patogeno)
    fun recuperarEspecie(id: Int): Especie
    fun recuperarPatogenoPorTipo(tipo: String): Patogeno
    fun recuperarPatogenoId(id: Int): Patogeno
    fun recuperarTodosLosPatogenos(): List<Patogeno>
    fun eliminarTodosLosPatogenos()
}