package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.evento.EventoArribo
import ar.edu.unq.eperdemic.excepciones.DuplicatedTypeException
import ar.edu.unq.eperdemic.excepciones.NotFoundException
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.dynamo.DynamoFeedDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongodb.MongoFeedDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.Neo4JUbicacionDAO
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import org.hibernate.exception.ConstraintViolationException
import javax.persistence.NoResultException
import javax.persistence.PersistenceException

class UbicacionServiceImpl(val ubicacionDAO: UbicacionDAO, val vectorService: VectorService) : UbicacionService {

    private val neo4jUbicacionDAO : Neo4JUbicacionDAO = Neo4JUbicacionDAO()
//    private val mongoDAO = MongoFeedDAO()
    private val dynamoDAO = DynamoFeedDAO()
    override fun mover(vectorId: Int, nombreUbicacion: String) {
        val vectorParaActualizar = vectorService.recuperarVector(vectorId)
        val vectoresEnLocacion = getVectoresPorLocacion(nombreUbicacion)
        val ubicacion = recuperarUbicacion(nombreUbicacion)
        if(puedeMover(vectorParaActualizar, ubicacion)) {
            var evento = EventoArribo()
            dynamoDAO.save(evento.eventoVectorViajes(vectorParaActualizar, ubicacion))
            vectorParaActualizar.ubicacionActual = ubicacion
            vectorService.moverYContagiar(vectorParaActualizar, vectoresEnLocacion)
        }
    }

    override fun expandir(nombreUbicacion: String) {
        var vectoresEnLocacion = getVectoresPorLocacion(nombreUbicacion)
        if(vectoresEnLocacion.isNotEmpty()) {
            var vRandom = getVectorRandom(vectoresEnLocacion)
            var vectoresEnLocacion = vectoresEnLocacion.toMutableList()
            vectoresEnLocacion.remove(vRandom)
            vectorService.contagiar(vRandom, vectoresEnLocacion)
        }
    }

    override fun crearUbicacion(nombreUbicacion: String): Ubicacion {
        var ubicacion = Ubicacion(nombreUbicacion)
        try {
            runTrx { ubicacionDAO.guardar(ubicacion); neo4jUbicacionDAO.guardar(ubicacion) }
        } catch(e:PersistenceException) { //¿?¿? Por qué no entra al catch del guardar?
            var cause = e.cause
            when(cause){
                is ConstraintViolationException ->
                    throw DuplicatedTypeException("La ubicación ya existe")
                else ->
                    throw e
            }
        }
        return ubicacion
    }

    override fun recuperarUbicacion(nombreUbicacion: String): Ubicacion {
        try {
            return runTrx { ubicacionDAO.recuperar(nombreUbicacion) }
        } catch (e: NoResultException) {
            throw NotFoundException("Ubicacion no encontrada")
        }
    }

    override fun recuperarATodos(): List<Ubicacion> {
        return runTrx { ubicacionDAO.recuperarATodos() }
    }

    fun getVectoresPorLocacion(nombreUbicacion: String): List<Vector> {
        val ubicacion = recuperarUbicacion(nombreUbicacion)
        return vectorService.recuperarPorUbicacion(ubicacion)
    }

    fun getVectorRandom(listaDeVectores: List<Vector>): Vector{
        return listaDeVectores.shuffled().take(1)[0]
    }

    override fun conectar(ubicacion1: String, ubicacion2: String, tipoCamino: String) {
        runTrx { neo4jUbicacionDAO.conectar(ubicacion1, ubicacion2, TipoDeCamino.valueOf(tipoCamino)) }
    }

    override fun conectados(nombreDeUbicacion: String): List<Ubicacion> {
        return runTrx { neo4jUbicacionDAO.conectados(nombreDeUbicacion) }
    }

    override fun puedeMover(vector: Vector, nuevaUbicacion: Ubicacion): Boolean {
        return runTrx { neo4jUbicacionDAO.puedeMover(vector, nuevaUbicacion) }
    }

    override fun capacidadDeExpansion(vectorId: Long, movimientos: Int): Int {
        var v = vectorService.recuperarVector(vectorId.toInt())
        var ret = 0
        runTrx { ret = neo4jUbicacionDAO.capacidadDeExpansion(v, movimientos) }
        return ret

    }

    override fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String){
        var vector = vectorService.recuperarVector(vectorId.toInt())
        var v = runTrx {
            neo4jUbicacionDAO.moverMasCorto(vector, nombreDeUbicacion)
        }
        vectorService.actualizar(v)
    }
}