package ar.edu.unq.eperdemic.modelo.eventos

import ar.edu.unq.eperdemic.modelo.enums.TipoEvento

class MutacionEvento : Evento {

    var tipoPatogeno: String = ""
    override var tipoEvento: String = TipoEvento.Mutacion.name

    protected constructor() {}

    constructor(descripcion: String) : super(descripcion) { }

    fun agregarActorPatogeno(tipoPatogeno: String){
        this.tipoPatogeno = tipoPatogeno
    }

    override fun obtenerPatogeno() = tipoPatogeno

    override fun obtenerUbicacion(): String {
        TODO("Objeto no soportado en clase Mutacion")
    }

    override fun obtenerVector(): Int {
        TODO("Objeto no soportado en clase Mutacion")
    }

    override fun obtenerVectorContagiado(): Int {
        TODO("Objeto no soportado en clase Mutacion")
    }

}