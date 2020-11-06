package ar.edu.unq.eperdemic.services.runner.impl

import ar.edu.unq.eperdemic.services.runner.providers.Neo4jSessionFactoryProvider
import ar.edu.unq.eperdemic.services.runner.interfaces.Transaction
import org.neo4j.driver.Session

class Neo4jTransaction : Transaction {

    private var session: Session? = null

    companion object {
        private var transaction: org.neo4j.driver.Transaction? = null

        val currentTransaction: org.neo4j.driver.Transaction
            get() {
                if (transaction == null) {
                    throw RuntimeException("No hay ninguna session en el contexto")
                }
                return transaction!!
            }
    }

    override fun start() {
        session = Neo4jSessionFactoryProvider.instance.createSession()
        transaction = session?.beginTransaction()
    }

    override fun commit() {
        transaction?.commit()
        session?.close()
    }

    override fun rollback() {
        // begintransaction implicit action
    }
}