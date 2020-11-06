package ar.edu.unq.eperdemic.dao.mongoDB.generic

import ar.edu.unq.eperdemic.services.runner.providers.MongoDBSessionFactoryProvider
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Sorts
import org.bson.conversions.Bson

open class GenericMongoDAO<T>(entityType: Class<T>) {

    protected var connection: MongoDBSessionFactoryProvider = MongoDBSessionFactoryProvider()
    protected var collection: MongoCollection<T>

    init {
        collection = connection.getCollection(entityType.simpleName, entityType)
    }

    fun deleteAll() {
        if(connection.session != null) {
            collection.drop(connection.session!!)
        }else{
            collection.drop()
        }
    }

    fun save(anObject: T) {
        save(listOf(anObject))
    }

    fun update(anObject: T, id: String?) {
        if(connection.session != null) {
            collection.replaceOne(connection.session!!, eq("id", id), anObject)
        }else{
            collection.replaceOne(eq("id", id), anObject)
        }
    }

    fun save(objects: List<T>) {
        if(connection.session != null) {
            collection.insertMany(connection.session!!, objects)
        }else{
            collection.insertMany(objects)
        }
    }

    operator fun get(id: String?): T? {
        return getBy("id", id)
    }

    operator fun get(id: Int?): T? {
        return getBy("id", id)
    }

    fun getBy(property:String, value: String?): T? {
        if(connection.session != null) {
            return collection.find(connection.session!!, eq(property, value)).first()
        }
        return collection.find(eq(property, value)).first()
    }

    fun getBy(property:String, value: Int?): T? {
        if(connection.session != null) {
            return collection.find(connection.session!!, eq(property, value)).first()
        }
        return collection.find(eq(property, value)).first()
    }

    fun <E> findEq(field:String, value:E ): List<T> {
        return find(eq(field, value))
    }

    fun find(filter:Bson): List<T> {
        if(connection.session != null) {
            return collection.find(connection.session!!, filter).into(mutableListOf())
        }
        return collection.find(filter).sort(Sorts.descending("fecha")).into(mutableListOf())
    }

    fun <T> aggregate(pipeline:List<Bson> , resultClass:Class<T>): List<T> {
        if(connection.session != null) {
            return collection.aggregate(connection.session!!, pipeline, resultClass).into(ArrayList())
        }
        return collection.aggregate(pipeline, resultClass).into(ArrayList())
    }

}