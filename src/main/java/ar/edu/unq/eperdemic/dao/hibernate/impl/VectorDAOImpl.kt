package ar.edu.unq.eperdemic.dao.hibernate.impl

import ar.edu.unq.eperdemic.dao.hibernate.interfaces.VectorDAO
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.dao.hibernate.generic.HibernateDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.exception.NoNameException
import ar.edu.unq.eperdemic.services.runner.impl.HibernateTransaction

@Suppress("DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES")
class VectorDAOImpl : HibernateDAO<Vector>(Vector::class.java), VectorDAO {


    override fun actualizar(vector: Vector) {
        val session = HibernateTransaction.currentSession
        session.update(vector)
    }

    override fun recuperarTodosVectores(): List<Vector> {
        val session = HibernateTransaction.currentSession
        val hql = ("from Vector ")
        val query = session.createQuery(hql, Vector::class.java)
        return query.resultList
    }

    override fun cantidadDeEspeciesEnUbicacion(nombreUbicacion: String, nombreEspecie: String): Int {
        val session = HibernateTransaction.currentSession
        val hql = """select count(e) from Vector v join v.especies e where e.nombre= :nombreEspecie and v.ubicacion.nombreUbicacion= :nombreUbicacion"""
        val query = session.createQuery(hql)
        query.setParameter("nombreUbicacion", nombreUbicacion)
        query.setParameter("nombreEspecie",nombreEspecie)
        return if (query.list().size > 0) query.singleResult.hashCode() else 0


    }

    override fun nombreDeUbicacion(id: Int): String {
        val session = HibernateTransaction.currentSession

        val hql = """select v.ubicacion.nombreUbicacion from Vector v where v.id= :id """
        val query = session.createQuery(hql).setParameter("id",id )

        return if (query.list().size > 0) query.singleResult.toString() else String()
    }

    override fun borrarVector(vectorId: Int) {
        val session = HibernateTransaction.currentSession
        val hql = ("delete from Vector where id = $vectorId")
        session.createNativeQuery(hql).executeUpdate()
    }

    override fun borrarTodosVector() {
        val session = HibernateTransaction.currentSession
        val hql = ("delete from Vector")
        session.createNativeQuery(hql).executeUpdate()
    }

    override fun actualizarVector(vector: Vector) {
        val session = HibernateTransaction.currentSession
        session.update(vector)

    }


    override fun recuperarPorTipo(tipo: String): Vector {
        val session = HibernateTransaction.currentSession
        val hql = (" from Vector v where v.tipoBiologico = :tipoBiologico")
        val query = session.createQuery(hql, Vector::class.java)
        query.setParameter("tipoBiologico", tipo).maxResults = 1


        return query.singleResult
    }

    override fun recuperarVector(vectorId: Int): Vector = try {
        this.recuperarVectorPorId(vectorId)
    } catch (e: Exception) {
        throw NoNameException("no existe el id $vectorId en la base de datos")
    }

    private fun recuperarVectorPorId(vectorId: Int): Vector {
        val vector: Vector = this.recuperar(vectorId)
        val tipoBiologico: String = this.recuperarTipoDeVector(vectorId)
        vector.setProbabilidad(ProbabilidadDeContagio())
        vector.setTipoBiologico(tipoBiologico)

        return vector
    }


    override fun recuperarTipoDeVector(id: Int): String {
        val session = HibernateTransaction.currentSession
        val hql = ("select tipoBiologico from Vector v where v.id = $id")
        val query = session.createQuery(hql)

        return if (query.list().size > 0) query.singleResult.toString() else String()
    }


    override fun actualizarVectores(toMutableList: MutableList<Vector>) = toMutableList.forEach { i -> this.actualizarVector(i) }

    override fun borrarTodasEspecies() {
        val session = HibernateTransaction.currentSession
        val hql = ("delete from vector_especie")
        session.createNativeQuery(hql).executeUpdate()
    }

    override fun recuperarEspecies(id: Int): MutableList<Especie> {
        val session = HibernateTransaction.currentSession
        val hql = " from Vector v where v.id=$id "
        val query = session.createQuery(hql, Vector::class.java)
        return query.singleResult.especies
    }
}