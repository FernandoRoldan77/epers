package ar.edu.unq.eperdemic.dao.hibernate.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie

interface EspecieDAO {
    fun guardar(especie: Especie)
    fun recuperar(id: Int?): Especie
    fun eliminarTodos()
    fun recuperarTodos(): List<Especie>
    fun actualizar(especie: Especie)
}