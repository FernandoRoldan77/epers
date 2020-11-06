package ar.edu.unq.eperdemic.dao.hibernate.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.AnalizadorDeDatos
import ar.edu.unq.eperdemic.modelo.entities.ReporteDeContagios
import ar.edu.unq.eperdemic.modelo.exception.NoNameException
import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction
import javax.persistence.NoResultException

class AnalizadorDeDatosImp : AnalizadorDeDatos {

    override fun cantidadEnUnaUbicacion(nombre: String): Int {
        val session = HibernateTransaction.currentSession
        val hql = """select count (v) from Ubicacion u join u.vectores v where u.nombreUbicacion= :nombre"""
        val query = session.createQuery(hql).setParameter("nombre", nombre)
        return if (query.list().size > 0) query.singleResult.hashCode() else 0
    }

    override fun cantidadInfectadosEnUnaUbicacion(nombre: String): Int {
        try {
            val session = HibernateTransaction.currentSession
            this.existeUbicacion(nombre)
            val hql = """select count (v) from Ubicacion u join u.vectores v where v.estaContagiado= TRUE and u.nombreUbicacion= :nombre"""
            val query = session.createQuery(hql)
            query.setParameter("nombre", nombre)
            return if (query.list().size > 0) query.singleResult.hashCode() else 0
        } catch (e: NoResultException) {
            throw NoNameException("no existe la ubicacion $nombre en la base de datos")
        }
    }

    override fun especieMasLetalEnUbicacion(nombre: String): String {
        try {
            val session = HibernateTransaction.currentSession
            this.existeUbicacion(nombre)
            val hql = """select e.nombre from Ubicacion u join u.vectores v join v.especies e join e.patogeno p where u.nombreUbicacion= :nombre group by e order by max(p.letalidad) desc """
            val query = session.createQuery(hql).setParameter("nombre", nombre).setMaxResults(1)

            return if (query.list().size > 0) query.singleResult.toString() else String()
        } catch (e: NoResultException) {
            throw NoNameException("no existe la ubicacion $nombre en la base de datos")
        }
    }

    override fun reporteDeContagios(nombre: String): ReporteDeContagios {
        try {
            this.existeUbicacion(nombre)
            val vectoresPresentes = this.cantidadEnUnaUbicacion(nombre)
            val vectores = this.cantidadInfectadosEnUnaUbicacion(nombre)
            val especieMasInfecciosa = this.especieMasLetalEnUbicacion(nombre)


            return ReporteDeContagios(vectoresPresentes, vectores, especieMasInfecciosa)
        } catch (e: NoResultException) {
            throw NoNameException("no existe la ubicacion $nombre en la base de datos")
        }
    }

    private fun existeUbicacion(nombre: String): Int {
        val session = HibernateTransaction.currentSession
        val hql = """select count(u) from Ubicacion u where u.nombreUbicacion= :nombre """
        val query = session.createQuery(hql).setParameter("nombre", nombre)

        return if (query.singleResult.hashCode() != 0) query.singleResult.hashCode() else throw NoNameException("no existe la ubicacion $nombre en la base de datos")
    }


}