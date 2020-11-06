package ar.edu.unq.eperdemic.dao.hibernate.generic

import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction

open class HibernateDAO<T>(private val entityType: Class<T>) {

    fun guardar(item: T) {
        val session = HibernateTransaction.currentSession
        session.save(item)
    }

    fun recuperar(id: Int?): T {
        val session = HibernateTransaction.currentSession
        return session.get(entityType, id)
    }
}