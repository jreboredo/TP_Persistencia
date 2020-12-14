package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.evento.Evento
import ar.edu.unq.eperdemic.persistencia.dao.dynamo.DynamoFeedDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongodb.MongoFeedDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.Neo4JUbicacionDAO
import ar.edu.unq.eperdemic.services.FeedService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class FeedServiceImpl : FeedService {
//    private val feedDAO: MongoFeedDAO = MongoFeedDAO()
    private val feedDAO: DynamoFeedDAO = DynamoFeedDAO()
    private val neo4JUbicacionDAO = Neo4JUbicacionDAO()

    override fun feedPatogeno(tipoDePatogeno: String): List<Evento> {
        return runTrx {
            feedDAO.getEventosEspecie(tipoDePatogeno)
        }
    }

    override fun feedVector(vectorId: Long): List<Evento> {
        return runTrx {
            feedDAO.getEventosVector(vectorId.toInt())
        }
    }

    override fun feedUbicacion(nombreDeUbicacion: String): List<Evento> {
        return runTrx {
            var ubicaciones :List<String> = listOf(nombreDeUbicacion).plus(neo4JUbicacionDAO.conectados(nombreDeUbicacion).map { u -> u.nombre!! })
            feedDAO.getEventosUbicaciones(ubicaciones)
        }
    }
}