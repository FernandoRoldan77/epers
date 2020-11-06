package ar.edu.unq.eperdemic.dao.hibernate.interfaces

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector

interface VectorDAO {
    fun guardar(vector: Vector)
    fun recuperar(vectorId: Int?): Vector
    fun borrarVector(vectorId: Int)
    fun borrarTodosVector()
    fun actualizarVector(vector: Vector)
    fun borrarTodasEspecies()
    fun recuperarPorTipo(tipo: String): Vector
    fun actualizarVectores(toMutableList: MutableList<Vector>)
    fun recuperarTipoDeVector(id: Int): String
    fun recuperarVector(vectorId: Int): Vector
    fun recuperarEspecies(id: Int): MutableList<Especie>
    fun actualizar(vector: Vector)
    fun recuperarTodosVectores(): List<Vector>
    fun cantidadDeEspeciesEnUbicacion(nombreUbicacion: String, nombreEspecie: String): Int
    fun nombreDeUbicacion(id: Int): String

}