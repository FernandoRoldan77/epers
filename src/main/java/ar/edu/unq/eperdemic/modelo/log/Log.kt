package ar.edu.unq.eperdemic.modelo.log

import org.bson.codecs.pojo.annotations.BsonProperty

class Log(var nombre: String) {
    @BsonProperty("id")
    val id: String? = null
}