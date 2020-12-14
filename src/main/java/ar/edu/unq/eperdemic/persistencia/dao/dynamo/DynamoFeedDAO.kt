package ar.edu.unq.eperdemic.persistencia.dao.dynamo

import ar.edu.unq.eperdemic.evento.Evento
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import java.util.HashMap

class DynamoFeedDAO: GenericDynamoDAO<Evento>(Evento::class.java) {

    fun getEventosUbicaciones(listaNombreUbicaciones : List<String>): List<Evento> {
        val expAttrValues: MutableMap<String, AttributeValue> = HashMap()
        //Negrada, pero necesaria... Tendría que haberme puesto duro y usar el CrudRepo aunque no este al 100% con dynamo..
        for (i in listaNombreUbicaciones.indices)
            expAttrValues[":${i}"] = AttributeValue(listaNombreUbicaciones[i])

        val filterExp = "nombreUbicacion IN (${expAttrValues.keys.joinToString()})"

        val scanExpression = DynamoDBScanExpression().withFilterExpression(filterExp).withExpressionAttributeValues(expAttrValues)
        return dynamoDBmapper.scan(Evento::class.java, scanExpression)
    }

    fun getEventosEspecie(tipoDePatogeno: String) : List<Evento>{
        val expAttrValues: MutableMap<String, AttributeValue> = HashMap()
        expAttrValues[":tipoPat"] = AttributeValue(tipoDePatogeno)
        val queryExpression = DynamoDBQueryExpression<Evento>()
                                .withIndexName("idx_global_idPat")
                                .withKeyConditionExpression("idPatogeno = :tipoPat")
                                .withExpressionAttributeValues(expAttrValues)
                                .withConsistentRead(false)
        return dynamoDBmapper.query(Evento::class.java, queryExpression)
    }

    fun getEventosVector(idVector: Int) : List<Evento>{
        val expAttrValues: MutableMap<String, AttributeValue> = HashMap()
        expAttrValues[":idVector"] = AttributeValue().withN(idVector.toString())
        val queryExpression = DynamoDBQueryExpression<Evento>()
                                .withIndexName("idx_global_idVector")
                                .withKeyConditionExpression("idVector = :idVector")
                                .withExpressionAttributeValues(expAttrValues)
                                .withConsistentRead(false)
        return dynamoDBmapper.query(Evento::class.java, queryExpression)
    }
}

//@EnableScan
//interface FeedRepository : CrudRepository<Evento, String> {
//    fun findByNombreUbicacionIn(lastName : String) : List<Evento>
//}