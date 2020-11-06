package ar.edu.unq.eperdemic.dao.hibernate.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.MutacionDAO
import ar.edu.unq.eperdemic.modelo.entities.mutacion.Mutacion
import ar.edu.unq.eperdemic.dao.hibernate.generic.HibernateDAO
import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction

class MutacionHibernateDAO : HibernateDAO<Mutacion>(Mutacion::class.java), MutacionDAO {

    override fun crear(mutacion: Mutacion): Int {
        this.guardar(mutacion)
        val session = HibernateTransaction.currentSession
        return session.getIdentifier(mutacion).hashCode()

    }

    override fun actualizar(mutacion: Mutacion) {
        val session = HibernateTransaction.currentSession
        session.update(mutacion)
    }

    override fun recuperarMutaciones(): List<Mutacion> {
        val session = HibernateTransaction.currentSession
        val hql = ("from Mutacion ")
        val query = session.createQuery(hql, Mutacion::class.java)
        return query.resultList
    }

    override fun eliminarMutaciones() {
        val session = HibernateTransaction.currentSession
        session.createQuery("delete from Mutacion")
                .executeUpdate()
    }
}