package ar.edu.unq.eperdemic.dao.hibernate.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.EspecieDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.dao.hibernate.generic.HibernateDAO
import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction


open class EspecieHibernateDAO : HibernateDAO<Especie>(Especie::class.java), EspecieDAO {

    override fun eliminarTodos() {
        val session = HibernateTransaction.currentSession
        session.createQuery("delete from Especie")
                .executeUpdate()
    }

    override fun recuperarTodos(): List<Especie> {
        val session = HibernateTransaction.currentSession
        val hql = ("from Especie")
        val query = session.createQuery(hql, Especie::class.java)
        return query.resultList
    }

    override fun actualizar(especie: Especie) {
        val session = HibernateTransaction.currentSession
        session.update(especie)
    }


}