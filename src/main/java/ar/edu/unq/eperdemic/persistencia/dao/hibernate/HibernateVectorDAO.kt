package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.runner.HibernateTransaction

class HibernateVectorDAO: HibernateDAO<Vector>(Vector:: class.java), VectorDAO {

    override fun borrar(id: Int) {
        val session = HibernateTransaction.currentSession
        val vector = this.recuperar(id)
        session.delete(vector)
    }

    override fun recuperarVectoresPorUbicacion(ubicacion: Ubicacion): List<Vector> {
        val session = HibernateTransaction.currentSession
        var hql = """select vector
                    from Vector vector
                    join vector.ubicacionActual as loc
                    where loc = :ubicacion
        """
        val query = session.createQuery(hql,Vector::class.java)
        query.setParameter("ubicacion", ubicacion)

        return query.list()
    }

    override fun recuperarUbicacionesDeVectoresInfectadosCon(especie: Especie): List<Ubicacion> {
        val session = HibernateTransaction.currentSession

        val hql = """select vector.ubicacionActual
                from Vector vector
                    join vector.infecciones as inf
                    where inf = :especie
        """

        val query = session.createQuery(hql,Ubicacion::class.java)
        query.setParameter("especie", especie)
        return query.list()
    }

    override fun cantidadDeVectoresInfectadosEn(nombreUbicacion: String): List<Vector> {
        val session = HibernateTransaction.currentSession

        val hql = """select vector
                from Vector vector
                    inner join vector.infecciones as inf
                    where vector.ubicacionActual.nombre = :ubicacion
        """
        val query = session.createQuery(hql,Vector::class.java)
        query.setParameter("ubicacion", nombreUbicacion)
        return query.resultList

    }


}