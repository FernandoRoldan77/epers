package ar.edu.unq.eperdemic.services.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector

interface VectorService {
    fun contagiar(vectorInfectado: Vector, vectores: List<Vector>)
    fun infectar(vector: Vector, especie: Especie)
    fun enfermedades(vectorId: Int): List<Especie>
    fun crear(vector: Vector)
    fun recuperarVector(vectorId: Int): Vector
    fun recuperarVectorPorTipoBIologico(tipo: String): Vector
    fun borrarVector(vectorId: Int)
    fun borrarTodos()
    fun borrarTodasLasespecies()
    fun actualizar(vector: Vector)
    fun recuperar(id: Int): Vector
    fun recuperarTodosVectores(): List<Vector>


}

