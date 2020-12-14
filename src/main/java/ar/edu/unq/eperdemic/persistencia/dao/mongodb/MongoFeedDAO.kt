package ar.edu.unq.eperdemic.persistencia.dao.mongodb

import ar.edu.unq.eperdemic.evento.Evento
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Indexes

class MongoFeedDAO: GenericMongoDAO<Evento>(Evento::class.java){

    fun getEventosUbicaciones(listaNombreUbicaciones : List<String>): List<Evento> {
        val match = Aggregates.match(`in`("nombreUbicacion", listaNombreUbicaciones))
        val sort = Aggregates.sort(Indexes.ascending("currentTime"))
        return aggregate(listOf(match, sort), Evento::class.java)
    }

    fun getEventosEspecie(tipoDePatogeno: String) : List<Evento>{
        val match = Aggregates.match(eq("idPatogeno", tipoDePatogeno))
        val sort = Aggregates.sort(Indexes.ascending("currentTime"))
        return aggregate(listOf(match, sort), Evento::class.java)
    }

    fun getEventosVector(idVector: Int) : List<Evento>{
        val match = Aggregates.match(eq("idVector", idVector))
        val sort = Aggregates.sort(Indexes.ascending("currentTime"))
        return aggregate(listOf(match, sort), Evento::class.java)
    }

}