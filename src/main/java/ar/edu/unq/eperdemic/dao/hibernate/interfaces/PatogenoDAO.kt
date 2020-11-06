package ar.edu.unq.eperdemic.dao.hibernate.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno

interface PatogenoDAO {
    fun guardar(patogeno: Patogeno)
    fun recuperar(id: Int?): Patogeno
    fun actualizar(patogeno: Patogeno)
    fun recuperarPorTipo(tipo: String): Patogeno
    fun recuperarATodos(): List<Patogeno>
    fun eliminarTodos()
    fun cantidadDeInfectados(especieid: Int): Int
    fun esPandemia(especieId: Int): Boolean
    fun recuperarEspecie(id: Int): Especie
}