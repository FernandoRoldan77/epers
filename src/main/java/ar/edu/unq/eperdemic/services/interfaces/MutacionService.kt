package ar.edu.unq.eperdemic.services.interfaces

import ar.edu.unq.eperdemic.modelo.entities.mutacion.Mutacion

interface MutacionService {
    fun mutar (especieId: Int, mutacionId: Int)
    fun crearMutacion(mutacion: Mutacion)
    fun recuperarMutacion(id: Int): Mutacion
    fun recuperarMutaciones(): List<Mutacion>
    fun actualizarMutacion(mutacion: Mutacion)
    fun eliminarTodaslasMutaciones()

}