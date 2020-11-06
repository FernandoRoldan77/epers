package ar.edu.unq.eperdemic.modelo.eventos

import ar.edu.unq.eperdemic.modelo.enums.TipoEvento

class ContagioEvento : Evento {
    var tipoPatogeno: String = ""
    var nombreUbicacion: String = ""
    var idVector: Int = 0
    var idVectorContagiado: Int = 0

    override var tipoEvento: String = TipoEvento.Contagio.name

    protected constructor() {}

    constructor(descripcion: String) : super(descripcion) { }

    fun agregarActorPatogeno(tipoPatogeno: String){
        this.tipoPatogeno = tipoPatogeno
    }

    fun agregarActorVector(idVector: Int){
        this.idVector = idVector
    }

    fun agregarActorVectorContagiado(idVector: Int){
        this.idVectorContagiado = idVector
    }

    fun agregarActorUbicacion(nombre: String) {
        this.nombreUbicacion = nombre
    }

    override fun obtenerUbicacion() = nombreUbicacion

    override fun obtenerVector() = idVector

    override fun obtenerPatogeno() = tipoPatogeno

    override fun obtenerVectorContagiado() = idVectorContagiado
}