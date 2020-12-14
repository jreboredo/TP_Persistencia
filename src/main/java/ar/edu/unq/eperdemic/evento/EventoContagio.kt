package ar.edu.unq.eperdemic.evento
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import org.bson.codecs.pojo.annotations.BsonDiscriminator

//@BsonDiscriminator("EventoContagio")
class EventoContagio : Evento() {




}