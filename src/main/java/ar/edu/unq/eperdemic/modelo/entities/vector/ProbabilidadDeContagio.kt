
package ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad

import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import kotlin.random.Random

class ProbabilidadDeContagio {


    fun evaluarProbabilidadDeContagioYCambiarEstadoDeSalud(vectorAContagiar: Vector) {
        val probabilidadDeContagio= probabilidadDeContagio(vectorAContagiar)
        vectorAContagiar.estaContagiado= esExitoso(probabilidadDeContagio)
    }

    fun probabilidadDeContagio(vectorAContagiar: Vector):Int{
        val factorContagio=calcularFactorContagio(vectorAContagiar.getTipoBiologico(),vectorAContagiar.especies)
        return Random.nextInt(1,10) + factorContagio
    }

    fun calcularFactorContagio(tipoBiologico: String, especies: MutableList<Especie>): Int {
        var contagio=especies.map {e->e.cantidadDeContagioPorTipoBiologico(tipoBiologico)}.sum()
        return contagio
    }

    fun numeroAleatorio():Int = Random.nextInt(1,100)
    fun esExitoso(probabilidadDeContagio: Int):Boolean= probabilidadDeContagio >=  numeroAleatorio()

    fun esContagioExitoso(vectorAContagiar: Vector): Boolean {
        val probabilidadDeContagio= probabilidadDeContagio(vectorAContagiar)
        return esExitoso(probabilidadDeContagio)
    }
}
