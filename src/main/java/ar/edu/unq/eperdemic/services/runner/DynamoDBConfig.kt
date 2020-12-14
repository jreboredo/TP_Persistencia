package ar.edu.unq.eperdemic.services.runner

import com.amazonaws.auth.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class DynamoDBConfig {
    //@Value("\${amazon.aws.accesskey}")
    private val amazonAWSAccessKey: String = "AKIAYRETGDE7N76S4I5Y"

    //@Value("\${amazon.aws.secretkey}")
    private val amazonAWSSecretKey: String = "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"

    fun amazonAWSCredentialsProvider(): AWSCredentialsProvider = AWSStaticCredentialsProvider(amazonAWSCredentials())

    @Bean
    fun amazonAWSCredentials(): AWSCredentials = BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey)

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(amazonAWSCredentialsProvider())
                .withRegion(Regions.SA_EAST_1).build()
}