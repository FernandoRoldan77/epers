package ar.edu.unq.eperdemic.services.interfaces

import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion

interface UbicacionService {
    fun mover(vectorId: Int, nombreUbicacion: String)
    //CRUD
    fun expandir(nombreUbicacion: String)
    fun crear(nombreUbicacion: String): Ubicacion
    fun actualizarUbicacion(ubicacion: Ubicacion)
    fun recuperar(id: Int): Ubicacion
    fun recuperarPorNombre(nombreUbicacion: String): Ubicacion
    fun eliminarUbicacion(ubicacion: Ubicacion)
    fun recuperarTodasLasUbicaciones() : List <Ubicacion>
    fun eliminarTodasLasUbicaciones()
    fun conectar(ubicacion1:String, ubicacion2:String, tipoCamino:String)
    fun conectados(nombreDeUbicacion: String): List<Ubicacion>
    fun moverMasCorto(vectorId:Int, nombreDeUbicacion:String)
    fun caminoEntreDosUbicacionesLindantes(nombreDeUbicacionOrigen: String, nombredeUbicacionDestino: String): String
    fun cantidadaDeCaminosPorTipoDeCamino(nombreDeLaUbicacionIngresada: String, tipoDeCaminoIngresado: String): Int
    fun capacidadDeExpansion(vectorId: Int, movimientos:Int): Int
    fun costoDeCaminoMasCorto(nombreUbicacion: String, tipoDeCaminoIngresado: String): Int
    fun esUbicacionMuyLejana(ubicacionOrigen: String, ubicacionDestino: String): Boolean
    fun esUbicacionAlcanzable(ubicacionOrigen: String, ubicacionDestino: String, caminos: List<String>): Boolean
    fun cantidadDeEspeciesEnUnaUbicacion(nombreUbicacion: String,nombreEspecie:String):Int
}