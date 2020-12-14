package ar.edu.unq.eperdemic.evento

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import com.amazonaws.services.dynamodbv2.datamodeling.*
import java.time.LocalDateTime

//@BsonDiscriminator
@DynamoDBTable(tableName = "Evento")
open class Evento {
//    @BsonProperty(value = "id")
    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey
    var id: String? = null

    @DynamoDBAttribute
    var log: String = ""

    @DynamoDBRangeKey
    @DynamoDBAttribute
    @DynamoDBIndexRangeKey(globalSecondaryIndexNames = ["idx_global_idVector", "idx_global_idPat"])
    lateinit var currentTime: String

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName="idx_global_idVector")
    var idVector: Long? = null

    @DynamoDBAttribute
    var nombreUbicacion: String? = null

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName="idx_global_idPat")
    var idPatogeno: String? = null

    constructor(){
        setCurrentTime()
    }

    fun setCurrentTime(){
        this.currentTime = LocalDateTime.now().toString()
    }

    fun eventoVectorViajes(vector: Vector, ubicacion: Ubicacion): Evento {
        this.log = "El vector con id: ${vector.id} se movio a la ubicacion: ${ubicacion.nombre}"
        this.idVector = vector.id?.toLong()
        this.nombreUbicacion = ubicacion.nombre
        return this
    }

    fun eventoPandemia(patogeno: Patogeno, especie: Especie): Evento {
        this.log = "La Especie: ${especie.nombre} perteneciento al Patogeno: ${patogeno.tipo} se ha convertido en pandemia"
        this.idPatogeno = patogeno.tipo
        return this
    }

    fun eventoEspecieEnNuevaUbicacion(especie: Especie, ubicacion: Ubicacion): Evento {
        this.nombreUbicacion = ubicacion.nombre
        this.idPatogeno = especie.patogeno?.tipo
        this.log = "La Especie: ${especie.nombre} perteneciento al Patogeno: ${especie.patogeno!!.tipo} ahora se encuentra tambien en: ${ubicacion.nombre}"
        return this
    }

    fun eventoEnfermedadEsPadecidaPor(vector: Vector, patogeno: Patogeno): Evento {
        this.log = "El Vector: ${vector.id} esta infectado por el patogeno: ${patogeno.tipo}"
        this.idVector = vector.id!!.toLong()
        this.idPatogeno = patogeno.tipo
        return this
    }
    fun elVectorPadeceUnaNuevaEnfermedad(vectorTransmisor: Vector, vectorContagiado: Vector): Evento {
        this.log = "El Vector: ${vectorTransmisor.id} ha infectado al vector ${vectorContagiado.id}"
        this.idVector = vectorTransmisor.id!!.toLong()
        return this
    }

    fun eventoEspecieCreada(patogeno: Patogeno, especie: Especie): Evento{
        this.log = "Se creo la Especie: ${especie.nombre} en el Patogeno:  ${patogeno.tipo}"
        this.idPatogeno = patogeno.tipo
        return this
    }

    fun eventoEspecieMuto(patogeno: Patogeno, especie: Especie): Evento {
        this.log = "La Especie: ${especie.nombre} perteneciento al Patogeno: ${patogeno.tipo} ha mutado"
        this.idPatogeno = patogeno.tipo
        return this
    }
}