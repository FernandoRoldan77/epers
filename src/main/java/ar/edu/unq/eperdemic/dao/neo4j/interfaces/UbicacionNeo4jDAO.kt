package ar.edu.unq.eperdemic.dao.neo4j.interfaces

import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion


interface UbicacionNeo4jDAO {
    fun crear(nombreUbicacion: String)
    fun conectarUbicaciones(ubicacion1: String, ubicacion2: String, tipoCamino: String)
    fun conectados(nombreDeUbicacion: String): List<Ubicacion>
    fun caminoMasCorto(ubicacionOrigen: String, ubicacionDestino: String): List<Ubicacion>
    fun getCaminoEntreDosUbicacionesLindantes(nombreDeUbicacionOrigen: String, nombredeUbicacionDestino: String): String
    fun cantidadDeCaminosDeTipo(nombreDeLaUbicacionIngresada: String, tipoDeCaminoIngresado: String): Int
    fun cantidadDeCaminosPosiblesSegunMovientos(nombreUbicacion: String, caminos: List<String>, movimientos: Int): Int
    fun cantidadDeCaminoMasCorto(ubicacionOrigen: String, ubicacionDestino: String): Int
    fun esUbicacionAlcanzable(ubicacionOrigen: String, ubicacionDestino: String, tiposDeCaminos: List<String>): Boolean
    fun esUbicacionMuyLejana(ubicacionOrigen: String, ubicacionDestino: String): Boolean
    fun eliminarTodo()
}