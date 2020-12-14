package ar.edu.unq.eperdemic.persistencia.dao.dynamo

import ar.edu.unq.eperdemic.services.runner.DynamoDBConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig



open class GenericDynamoDAO<T>(private val entityType: Class<T>) {

    private val dynamoDB = DynamoDBConfig().amazonDynamoDB()

    val dynamoDBmapper : DynamoDBMapper

    init {
        val dynamoDBMapperConfig = DynamoDBMapperConfig.Builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.EVENTUAL)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE)
                .build()
        dynamoDBmapper =DynamoDBMapper(dynamoDB, dynamoDBMapperConfig)
    }

    fun save(anObject: T): T {
        dynamoDBmapper.save(anObject)
        return anObject
    }

    fun save(objects: List<T>): List<T> {
        dynamoDBmapper.batchSave(objects)
        return objects
    }

    fun getById(id: String?) :T? {
       return dynamoDBmapper.load(entityType, id)
    }

}