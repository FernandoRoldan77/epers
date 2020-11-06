package ar.edu.unq.eperdemic.dao.hibernate.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.EstadisticaDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction

class EstadisticaHibernateDAO : EstadisticaDAO {
    override fun especieLider(): Especie {
        val session = HibernateTransaction.currentSession

        val hql = """  select e from Especie e join e.vectores v where v.tipoBiologico= 'Humano' group by e order by count(e.id) desc """
        val query = session.createQuery(hql, Especie::class.java).setMaxResults(1)

        return query.singleResult
    }

    override fun lideres(): List<Especie> {
        val session = HibernateTransaction.currentSession

        val hql = """  select e from Especie e join e.vectores v where v.tipoBiologico= 'Humano' or v.tipoBiologico= 'Animal' group by e order by count(e.id) desc"""

        val query = session.createQuery(hql, Especie::class.java)
        query.maxResults = 10
        return query.resultList
    }
}