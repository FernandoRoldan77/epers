package ar.edu.unq.eperdemic.services.runner

import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction
import ar.edu.unq.eperdemic.services.runner.impl.Neo4jTransaction
import ar.edu.unq.eperdemic.services.runner.interfaces.Transaction

object TransactionRunner {
    private var transactions: MutableList<Transaction> = mutableListOf(HibernateTransaction(), Neo4jTransaction())

    fun <T> runTrx(bloque: () -> T): T {
        try {
            transactions.forEach { it.start() }
            val result = bloque()
            transactions.forEach { it.commit() }
            return result
        } catch (exception: Throwable) {
            transactions.forEach { it.rollback() }
            throw exception
        }
    }

}