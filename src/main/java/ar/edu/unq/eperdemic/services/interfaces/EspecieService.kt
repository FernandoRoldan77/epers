package ar.edu.unq.eperdemic.services.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie

interface EspecieService {
    fun crear(especie: Especie)
    fun actualizar(especie: Especie)
    fun recuperar(id: Int?): Especie
    fun recuperarTodos(): List<Especie>
    fun eliminarTodos()
}

