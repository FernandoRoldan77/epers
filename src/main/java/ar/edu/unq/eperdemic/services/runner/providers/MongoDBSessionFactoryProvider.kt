package ar.edu.unq.eperdemic.services.runner.providers

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCommandException
import com.mongodb.client.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider


class MongoDBSessionFactoryProvider {

    private var client: MongoClient
    private var dataBase: MongoDatabase
    var session: ClientSession? = null

    init {
        val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )
        val uri = System.getenv().getOrDefault("MONGO_URI", "mongodb://localhost:27017")
        val connectionString = ConnectionString(uri)
        val settings = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .applyConnectionString(connectionString)
                .build()
        client = MongoClients.create(settings)
        dataBase = client.getDatabase("eperdemic")
    }

    fun createSession(): ClientSession {
        return this.client.startSession()
    }

    fun getInstanceDataBase(): MongoClient {
        return this.client
    }

    fun <T> getCollection(name:String, entityType: Class<T> ): MongoCollection<T> {
        try{
            dataBase.createCollection(name)
        } catch (exception: MongoCommandException){
            println("Ya existe la coleccion $name")
        }
        return dataBase.getCollection(name, entityType)
    }

    companion object {



        private var INSTANCE: MongoDBSessionFactoryProvider? = null

        val instance: MongoDBSessionFactoryProvider
            get() {
                if (INSTANCE == null) {
                    INSTANCE = MongoDBSessionFactoryProvider()
                }
                return INSTANCE!!
            }

        fun destroy() {
            if (INSTANCE != null && INSTANCE!!.session != null) {
                INSTANCE!!.session!!.close()
            }
            INSTANCE = null
        }
    }
}