package ar.edu.unq.eperdemic.modelo.eventos

import org.bson.codecs.pojo.annotations.BsonDiscriminator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

@BsonDiscriminator
abstract class Evento {
    @BsonProperty("_id")
    @BsonId
    var id: ObjectId? = null
    abstract var tipoEvento: String
    lateinit var fecha: String
    lateinit var descripcion: String

    protected constructor() {}

    constructor(descripcion: String) {
        this.descripcion = descripcion
        val fechaHoraActual = Date().time
        this.fecha = SimpleDateFormat("dd/M/yyyy hh:mm:ss.SSS").format(fechaHoraActual)
    }

    abstract fun obtenerUbicacion(): String
    abstract fun obtenerVector(): Int
    abstract fun obtenerPatogeno(): String

    abstract fun obtenerVectorContagiado(): Int
}