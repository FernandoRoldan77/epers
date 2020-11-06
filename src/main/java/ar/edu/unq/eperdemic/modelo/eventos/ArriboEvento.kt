package ar.edu.unq.eperdemic.modelo.eventos

import ar.edu.unq.eperdemic.modelo.enums.TipoEvento

class ArriboEvento : Evento {

    var idVector: Int = 0
    var nombreUbicacion: String = ""
    override var tipoEvento: String = TipoEvento.Arribo.name

    protected constructor() {}

    constructor(descripcion: String) : super(descripcion) { }

    fun agregarActorUbicacion(nombreUbicacion: String){
        this.nombreUbicacion = nombreUbicacion
    }

    fun agregarActorVector(idVector: Int){
        this.idVector = idVector
    }

    override fun obtenerUbicacion() = nombreUbicacion

    override fun obtenerVector() = idVector

    override fun obtenerPatogeno(): String {
        TODO("Objeto no soportado en clase Arribo")
    }

    override fun obtenerVectorContagiado(): Int {
        TODO("Objeto no soportado en clase Arribo")
    }

}