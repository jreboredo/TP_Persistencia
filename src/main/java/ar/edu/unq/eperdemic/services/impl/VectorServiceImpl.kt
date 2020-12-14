package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.evento.Evento
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.excepciones.NotFoundException
import ar.edu.unq.eperdemic.persistencia.dao.dynamo.DynamoFeedDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongodb.MongoFeedDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import kotlin.Exception

class VectorServiceImpl(private val vectorDAO: VectorDAO, private val especieDAO: EspecieDAO, val patogenoService: PatogenoService) : VectorService {


    /* Operaciones CRUD */

//    private val mongoDAO = MongoFeedDAO()
      private val dynamoDAO = DynamoFeedDAO()
    override fun crearVector(vector: Vector): Int {
        runTrx { vectorDAO.guardar(vector) }
        return vector.id!!
    }

    override fun recuperarVector(vectorId: Int): Vector {
        return runTrx { vectorDAO.recuperar(vectorId) }
    }

    override fun borrarVector(vectorId: Int) {
        try {
            runTrx {
                    vectorDAO.borrar(vectorId)
                }
            } catch(e:Exception) {
            throw NotFoundException("Vector no existe")
        }
    }

    override fun actualizar(vector: Vector) {
        runTrx { vectorDAO.actualizar(vector) }
    }

    override fun actualizarNotransaccional(vector:Vector){
        vectorDAO.actualizar(vector)
    }

    override fun recuperarATodos(): List<Vector> {
        return runTrx { vectorDAO.recuperarATodos() }
    }

    override fun recuperarPorUbicacion(ubicacion: Ubicacion): List<Vector> {
        return runTrx { vectorDAO.recuperarVectoresPorUbicacion(ubicacion) }
    }

    override fun moverYContagiar(vectorInfectado: Vector, vectores: List<Vector>) {
        runTrx {
            vectorDAO.actualizar(vectorInfectado)
            intentarContagiar(vectorInfectado, vectores)
        }
    }

    override fun contagiar(vectorInfectado: Vector, vectores: List<Vector>) {
        runTrx { intentarContagiar(vectorInfectado, vectores) }
    }

    //versión no transaccional.
    private fun intentarContagiar(vectorInfectado: Vector, vectores: List<Vector>) {
        vectores.forEach {
            it.prevenirInfectarseASiMismo(vectorInfectado)
            contagiarUno(vectorInfectado, it)
        }
    }

    private fun contagiarUno(vInfectado: Vector, vObjetivo: Vector) {
        if (vObjetivo.esDeTipoValidoParaInfectar(vInfectado)) {
            vInfectado.infecciones.forEach {
                if((it.puedeContagiarA(vObjetivo))){
                    infectarPorContagio(vObjetivo, vInfectado, it)
                    val eventToSave = Evento().eventoEnfermedadEsPadecidaPor(vObjetivo, it.patogeno!!)
                    dynamoDAO.save(eventToSave)
                }
            }
        }
    }

    private fun infectarPorContagio(vector: Vector, vectorInfectado: Vector, especie: Especie) {
        // Version no transaccional del infectar, para usar dentro de contagiar
            if(!(vector.infectadoCon(especie))) {
                vector.infectar(especie)
                especieDAO.actualizar(especie)
                vectorDAO.actualizar(vector)
                dynamoDAO.save(Evento().elVectorPadeceUnaNuevaEnfermedad(vectorInfectado, vector))
                if(especieDAO.especieSeEncuentraEn(vector.ubicacionActual!!)){
                    dynamoDAO.save(Evento().eventoEspecieEnNuevaUbicacion(especie,vector.ubicacionActual!!))
                }
            }
        if(!especie.esPandemia && patogenoService.esPandemia(especie.id!!)) {
            especie.esPandemia = true
            especieDAO.actualizar(especie)
            dynamoDAO.save(Evento().eventoPandemia(especie.patogeno!!,especie))
        }

    }

    override fun infectar(vector: Vector, especie: Especie) {
        try {
            runTrx {
                    vector.infectar(especie)
                    especieDAO.actualizar(especie)
                    vectorDAO.actualizar(vector)
                }
        } catch (e:Exception) {
            throw NotFoundException("No existe el Vector dado")
        }
    }

    override fun enfermedades(vectorId: Int): List<Especie> {
        try {
            val vector = runTrx { vectorDAO.recuperar(vectorId) }
            return vector.infecciones.toList()
        }
        catch (e: Exception) {
            throw NotFoundException("No existe el Vector dado")
        }
    }

    override fun recuperarUbicacionesDeVectoresInfectadosCon(especie: Especie): List<Ubicacion>{
        return runTrx { vectorDAO.recuperarUbicacionesDeVectoresInfectadosCon(especie) }
    }
}
