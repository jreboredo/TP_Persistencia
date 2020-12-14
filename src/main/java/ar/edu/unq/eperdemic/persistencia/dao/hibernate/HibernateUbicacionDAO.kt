package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.runner.HibernateTransaction

class HibernateUbicacionDAO : HibernateDAO<Ubicacion>(Ubicacion::class.java), UbicacionDAO {
    override fun recuperar(nombreDeUbicacion: String): Ubicacion {
        val session = HibernateTransaction.currentSession

        val hql = ("from Ubicacion u  where lower(u.nombre) like :pNombre")

        val query = session.createQuery(hql, Ubicacion::class.java)
        query.setParameter("pNombre", nombreDeUbicacion.toLowerCase())

        return query.singleResult
    }


}