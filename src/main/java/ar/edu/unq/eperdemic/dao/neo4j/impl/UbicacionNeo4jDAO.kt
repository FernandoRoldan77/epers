package ar.edu.unq.eperdemic.dao.neo4j.impl

import ar.edu.unq.eperdemic.dao.neo4j.interfaces.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.services.runner.impl.Neo4jTransaction
import org.neo4j.driver.Values

class UbicacionNeo4jDAO : UbicacionNeo4jDAO {

    override fun crear(nombreUbicacion: String) {
        val session = Neo4jTransaction.currentTransaction
        val query = "MERGE (n:Ubicacion {nombre: ${'$'}nombre })"
        session.run(query, Values.parameters(
                "nombre", nombreUbicacion
        ))
    }

    override fun conectados(nombreDeUbicacion: String): List<Ubicacion> {
        val session = Neo4jTransaction.currentTransaction
        val query = """MATCH (:Ubicacion {nombre: ${'$'}nombreDeUbicacion})-[]->(u2:Ubicacion) return u2"""

        return session.run(query, Values.parameters(
                "nombreDeUbicacion", nombreDeUbicacion
        )).list {
            val ubicacion = it.get(0)
            val nombre = ubicacion["nombre"].asString()
            Ubicacion(nombre)
        }
    }

    override fun conectarUbicaciones(ubicacion1: String, ubicacion2: String, tipoCamino: String) {
        val session = Neo4jTransaction.currentTransaction
        val query = """MATCH (u1:Ubicacion{nombre: ${'$'}ubicacion1})
                       MATCH (u2:Ubicacion{nombre: ${'$'}ubicacion2})
                       MERGE (u1)-[:${tipoCamino}]->(u2)"""
        session.run(query, Values.parameters(
                "ubicacion1", ubicacion1,
                "ubicacion2", ubicacion2
        ))
    }

    override fun esUbicacionAlcanzable(ubicacionOrigen: String, ubicacionDestino: String, tiposDeCaminos: List<String>): Boolean {
        val relaciones = tiposDeCaminos.joinToString("|")

        val session = Neo4jTransaction.currentTransaction
        val query = """MATCH (:Ubicacion { nombre: ${'$'}ubicacionOrigen })-[:$relaciones]->(u2:Ubicacion{ nombre: ${'$'}ubicacionDestino }) return count(u2) > 0"""

        return session.run(query, Values.parameters(
                "ubicacionOrigen", ubicacionOrigen,
                "ubicacionDestino", ubicacionDestino
        )).single().get(0).asBoolean()
    }

    override fun esUbicacionMuyLejana(ubicacionOrigen: String,
                                      ubicacionDestino: String): Boolean {
        val session = Neo4jTransaction.currentTransaction
        val query = """MATCH (:Ubicacion {nombre: ${'$'}ubicacionOrigen})
                        -[]->(u2:Ubicacion {nombre: ${'$'}ubicacionDestino})
                        return count(u2) = 0"""

        return session.run(query, Values.parameters(
                "ubicacionOrigen", ubicacionOrigen,
                "ubicacionDestino", ubicacionDestino
        )).single().get(0).asBoolean()
    }

    override fun caminoMasCorto(ubicacionOrigen: String, ubicacionDestino: String): List<Ubicacion> {
        val session = Neo4jTransaction.currentTransaction
        val query = "MATCH (u1:Ubicacion{nombre:${'$'}ubicacionOrigen}),(u2:Ubicacion{nombre:${'$'}ubicacionDestino}), p = shortestPath((u1)-[*]-(u2)) return p"
        val result = session.run(query, Values.parameters(
                "ubicacionOrigen", ubicacionOrigen,
                "ubicacionDestino", ubicacionDestino
        ))

        return if (result.hasNext()) {
            result.single().get(0).asPath().nodes().map {
                Ubicacion(it.get("nombre").asString())
            }
        } else mutableListOf()
    }

    override fun getCaminoEntreDosUbicacionesLindantes(ubicacionOrigen: String, ubicacionDestino: String): String {
        val session = Neo4jTransaction.currentTransaction
        val query = "MATCH (u1:Ubicacion{nombre:${'$'}ubicacionOrigen})-[r]- >(u2:Ubicacion{nombre:${'$'}ubicacionDestino}) return type(r)"
        return session.run(query, Values.parameters(
                "ubicacionOrigen", ubicacionOrigen,
                "ubicacionDestino", ubicacionDestino
        )).single().get(0).asString()
    }

    override fun cantidadDeCaminosDeTipo(ubicacionOrigen: String, tipoDeCaminoIngresado: String): Int {
        val session = Neo4jTransaction.currentTransaction
        val query = "MATCH t=(b:Ubicacion{nombre:${'$'}ubicacionOrigen})-[:$tipoDeCaminoIngresado]->() RETURN count(t)"
        return session.run(query, Values.parameters(
                "ubicacionOrigen", ubicacionOrigen,
                "ubicacionDestino", tipoDeCaminoIngresado
        )).single().get(0).asInt()
    }


    override fun cantidadDeCaminosPosiblesSegunMovientos(nombreUbicacion: String, caminosPosibles: List<String>, movimientos: Int): Int {
        val session = Neo4jTransaction.currentTransaction

        val caminos = this.crearQuery(caminosPosibles)
        val query = "MATCH t=(b:Ubicacion{nombre:${'$'}nombreUbicacion})-[k*0..${movimientos}]- >(e) where all(a in k where ${caminos}) RETURN count(distinct ID(e))-1"
        return session.run(query, Values.parameters(
                "nombreUbicacion", nombreUbicacion,
                "movimientos1", movimientos,
                "caminos", caminos)).single().get(0).asInt()
    }


    private fun crearQuery(caminosPosibles: List<String>): String = if (caminosPosibles.size == 1) "type(a)=${caminosPosibles[0]}" else this.matchQuery(caminosPosibles)

    private fun matchQuery(caminosPosibles: List<String>): String {
        val primero = "type(a)=" + "'" + "${caminosPosibles[0]}" + "'"
        val ls = caminosPosibles.subList(1, caminosPosibles.size)
        val res = ls.map { c -> " " + "or type(a)=" + "'" + "${c}" + "'" }
        return this.spliceStrings(primero, res)
    }

    private fun spliceStrings(primero: String, ls: List<String>): String {
        var res = primero
        ls.forEach { l -> res += l }
        return res
    }

    override fun cantidadDeCaminoMasCorto(ubicacionOrigen: String, ubicacionDestino: String): Int {
        val session = Neo4jTransaction.currentTransaction
        val query = "MATCH (u1:Ubicacion{nombre:${'$'}ubicacionOrigen}),(u2:Ubicacion{nombre:${'$'}ubicacionDestino}), p = shortestPath((u1)-[*]-(u2)) WITH reduce(output=0,n IN nodes(p)|output+ 1)as cantNode UNWIND cantNode as cant return distinct cant -1"
        return session.run(query, Values.parameters(
                "ubicacionOrigen", ubicacionOrigen,
                "ubicacionDestino", ubicacionDestino
        )).single().get(0).asInt()
    }

    override fun eliminarTodo() {
        val session = Neo4jTransaction.currentTransaction
        val query = "MATCH (u) DETACH DELETE u"
        session.run(query)
    }
}