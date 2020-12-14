package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.VectorType
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.services.runner.HibernateTransaction

class HibernateEspecieDAO : HibernateDAO<Especie>(Especie::class.java), EspecieDAO{

    override fun especieLider(): Especie{
        val session = HibernateTransaction.currentSession

        var hql = """
            select especie
            from Vector as vector
            inner join vector.infecciones as especie
            where vector.tipo = :tipo
            group by especie
            order by count(vector) desc
        """

        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("tipo", VectorType.Humano)
        query.setMaxResults(1)
        return query.singleResult

    }

    override fun especiesLideres(): List<Especie> {
        val session = HibernateTransaction.currentSession

        var hql = """
            select especie
            from Vector as vector
            inner join vector.infecciones as especie
            where vector.tipo = :tipo1 or vector.tipo = :tipo2
            group by especie
            order by count(vector) desc
        """
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("tipo1", VectorType.Humano)
        query.setParameter("tipo2", VectorType.Animal)
        query.maxResults = 10
        return query.resultList

    }

    override fun especieLiderTodoTipo(): Especie {
        val session = HibernateTransaction.currentSession

        var hql = """
            select especie
            from Vector as vector
            inner join vector.infecciones as especie
            group by especie
            order by count(vector) desc
            
        """

        val query = session.createQuery(hql, Especie::class.java)
        query.maxResults = 1
        return query.singleResult
    }

    override fun especieSeEncuentraEn(ubicacion: Ubicacion): Boolean {
        val session = HibernateTransaction.currentSession

        var hql = """
            select count(vector)
            from Vector as vector
            where vector.ubicacionActual.nombre = :ubicacion
        """

        val query = session.createQuery(hql, java.lang.Long::class.java)
        query.setParameter("ubicacion", ubicacion.nombre)
        return query.singleResult > 0
    }
}