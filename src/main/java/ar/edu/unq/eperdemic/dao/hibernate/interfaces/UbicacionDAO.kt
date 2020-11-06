package ar.edu.unq.eperdemic.dao.hibernate.interfaces

import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion

interface UbicacionDAO {
    fun crear(ubicacion: Ubicacion): Int
    fun actualizar(ubicacion: Ubicacion)
    fun eliminarUbicacion(ubicacion: Ubicacion)
    fun recuperar(id: Int?): Ubicacion
    fun recuperarATodos(): List<Ubicacion>
    fun eliminarUbicaciones()
    fun recuperarPorNombre(nombre: String): Ubicacion
    fun cantidadDeEspeciesEnUbicacion(nombreUbicacion: String, nombreEspecie: String): Int


}