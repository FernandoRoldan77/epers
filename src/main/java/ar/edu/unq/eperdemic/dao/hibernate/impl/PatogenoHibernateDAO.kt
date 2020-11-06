package ar.edu.unq.eperdemic.dao.hibernate.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.PatogenoDAO
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.dao.hibernate.generic.HibernateDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction


open class PatogenoHibernateDAO : HibernateDAO<Patogeno>(Patogeno::class.java), PatogenoDAO {

    override fun actualizar(patogeno: Patogeno) {
        val session = HibernateTransaction.currentSession
        session.update(patogeno)
    }

    override fun recuperarPorTipo(tipo: String): Patogeno {
        val session = HibernateTransaction.currentSession
        val hql = ("from Patogeno p where p.tipo = :tipo")
        val query = session.createQuery(hql, Patogeno::class.java)
        query.setParameter("tipo", tipo)

        return query.singleResult
    }

    override fun recuperarATodos(): List<Patogeno> {
        val session = HibernateTransaction.currentSession
        val hql = ("from Patogeno order by tipo")
        val query = session.createQuery(hql, Patogeno::class.java)

        return query.resultList
    }

    override fun eliminarTodos() {
        val session = HibernateTransaction.currentSession
        session.createQuery("delete from Patogeno")
                .executeUpdate()
    }

    override fun cantidadDeInfectados(especieid: Int): Int {

        val session = HibernateTransaction.currentSession
        val hql = """
                    SELECT COUNT(v.id)
                    FROM Especie e
                    INNER JOIN e.vectores v
                    WHERE e.id = :especieId AND v.estaContagiado = TRUE
        """.trimIndent()
        val query = session.createQuery(hql, Long::class.javaObjectType)
        query.setParameter("especieId", especieid)
        return query.singleResult.toInt()
    }

    override fun esPandemia(especieId: Int): Boolean {
        val session = HibernateTransaction.currentSession
        val hql = """
            SELECT COUNT(e.nombre)
            FROM Especie e
            INNER JOIN e.vectores v
            INNER JOIN v.ubicacion u
            WHERE e.id = :especieId
            GROUP BY e.nombre
            HAVING COUNT(DISTINCT u.nombreUbicacion) > (SELECT (COUNT(ui) / 2) FROM Ubicacion ui)
        """.trimIndent()
        val query = session.createQuery(hql, Long::class.javaObjectType)
        query.setParameter("especieId", especieId)
        return query.resultList.size > 0
    }

    override fun recuperarEspecie(id: Int): Especie {
        val session = HibernateTransaction.currentSession
        val hql = """from Patogeno p join p.especies e where e.id = :id"""
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("id", id)

        return query.singleResult
    }

}