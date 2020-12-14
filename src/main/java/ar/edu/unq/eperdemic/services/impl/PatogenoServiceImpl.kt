package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.evento.Evento
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.*
import ar.edu.unq.eperdemic.persistencia.dao.dynamo.DynamoFeedDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongodb.MongoFeedDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class PatogenoServiceImpl(val patogenoDAO: PatogenoDAO, val especieDAO : EspecieDAO, val ubicacionDAO: UbicacionDAO, val vectorDAO: VectorDAO) : PatogenoService {

//    val mongoDAO = MongoFeedDAO()
      val dynamoDAO = DynamoFeedDAO()
    override fun crearPatogeno(patogeno: Patogeno): Int {
        runTrx { patogenoDAO.guardar(patogeno)
        }
        return patogeno.id!!
    }

    override fun recuperarPatogeno(id: Int): Patogeno {
        return  runTrx { patogenoDAO.recuperar(id) }
    }

    override fun recuperarATodosLosPatogenos(): List<Patogeno> {
        return runTrx { patogenoDAO.recuperarATodos() }
    }

    override fun agregarEspecie(idPatogeno: Int, nombreEspecie: String, paisDeOrigen: String): Especie {
        return runTrx {
                val patogeno = patogenoDAO.recuperar(idPatogeno)
                val especie = patogeno.agregarEspecie(nombreEspecie, paisDeOrigen)
                patogenoDAO.actualizar(patogeno)
                dynamoDAO.save(Evento().eventoEspecieCreada(patogeno, especie))
                especie
            }
    }

    override fun actualizar(patogeno: Patogeno) {
        runTrx { patogenoDAO.actualizar(patogeno) }
    }

    override fun cantidadDeInfectados(especieId: Int): Int {
        return this.recuperarEspecie(especieId).cantidadDeContagios
    }

    override fun esPandemia(especieId: Int): Boolean {
        var especie = especieDAO.recuperar(especieId)
        var locacionesInfectadasConEspecie = vectorDAO.recuperarUbicacionesDeVectoresInfectadosCon(especie)
        var locaciones = ubicacionDAO.recuperarATodos()
        return locacionesInfectadasConEspecie.size >= (locaciones.size / 2)
    }

    override fun recuperarEspecie(id: Int): Especie {
        return runTrx { especieDAO.recuperar(id) }
    }

    override fun actualizarEspecie(especie: Especie) {
        runTrx { especieDAO.actualizar(especie) }
    }

}