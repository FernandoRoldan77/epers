package ar.edu.unq.eperdemic.dao.hibernate.interfaces

import ar.edu.unq.eperdemic.modelo.entities.mutacion.Mutacion

interface MutacionDAO {
    fun crear(mutacion: Mutacion): Int
    fun recuperar(id: Int?): Mutacion
    fun actualizar(mutacion: Mutacion)
    fun recuperarMutaciones(): List<Mutacion>
    fun eliminarMutaciones()
}