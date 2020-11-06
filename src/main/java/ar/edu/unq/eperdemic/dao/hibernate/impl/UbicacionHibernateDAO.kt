package ar.edu.unq.eperdemic.dao.hibernate.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.UbicacionDAO
import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.dao.hibernate.generic.HibernateDAO
import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction

open class UbicacionHibernateDAO : HibernateDAO<Ubicacion>(Ubicacion::class.java), UbicacionDAO {

    override fun crear(ubicacion: Ubicacion): Int {
        this.guardar(ubicacion)
        val session = HibernateTransaction.currentSession

        return session.getIdentifier(ubicacion).hashCode()
    }

    override fun actualizar(ubicacion: Ubicacion) {
        val session = HibernateTransaction.currentSession
        session.update(ubicacion)
    }

    override fun eliminarUbicacion(ubicacion: Ubicacion) {
        val session = HibernateTransaction.currentSession
        session.delete(ubicacion)
    }

    override fun recuperarATodos(): List<Ubicacion> {
        val session = HibernateTransaction.currentSession
        val hql = ("from Ubicacion ")
        val query = session.createQuery(hql, Ubicacion::class.java)
        return query.resultList
    }

    override fun eliminarUbicaciones() {
        val session = HibernateTransaction.currentSession
        val hql = ("delete from Ubicacion")
        session.createNativeQuery(hql).executeUpdate()
    }

    override fun recuperarPorNombre(nombre: String): Ubicacion {
        val session = HibernateTransaction.currentSession
        val hql = ("from Ubicacion where nombreUbicacion = :nombre")
        val query = session.createQuery(hql, Ubicacion::class.java)
        query.setParameter("nombre", nombre)

        return query.singleResult
    }

    override fun cantidadDeEspeciesEnUbicacion(nombreUbicacion: String, nombreEspecie: String): Int {
        val session = HibernateTransaction.currentSession
        val hql = """select count(e) from Ubicacion u join u.vectores v join v.especies e where e.nombre= :nombreEspecie and u.nombreUbicacion= :nombreUbicacion"""
        val query = session.createQuery(hql)
        query.setParameter("nombreUbicacion", nombreUbicacion)
        query.setParameter("nombreEspecie", nombreEspecie)
        return if (query.list().size > 0) query.singleResult.hashCode() else 0
    }

}