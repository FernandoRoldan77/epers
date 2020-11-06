package ar.edu.unq.eperdemic.services.runner.impl

import ar.edu.unq.eperdemic.services.runner.interfaces.Transaction
import ar.edu.unq.eperdemic.services.runner.providers.MongoDBSessionFactoryProvider
import com.mongodb.MongoClientException
import com.mongodb.TransactionOptions
import com.mongodb.WriteConcern
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoClient

class MongoDBTransaction : Transaction {

    private var session: ClientSession? = null

    companion object {
        private var client: MongoClient? = null

        val currentClient: MongoClient
            get() {
                if (client == null) {
                    throw RuntimeException("No hay ninguna session en el contexto")
                }
                return client!!
            }
    }

    override fun start() {
        try {
            session = MongoDBSessionFactoryProvider.instance.createSession()
            session?.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build())
            client = MongoDBSessionFactoryProvider.instance.getInstanceDataBase()
        } catch (exception: MongoClientException) {
            exception.printStackTrace()
        }
    }

    override fun commit() {
        session?.commitTransaction()
        closeSession()
    }

    override fun rollback() {
        session?.abortTransaction()
        closeSession()
    }

    protected fun closeSession() {
        session?.close()
        session = null
    }
}

