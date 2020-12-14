package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.evento.Evento
import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.dynamo.DynamoFeedDAO
import ar.edu.unq.eperdemic.services.FeedService
import ar.edu.unq.eperdemic.services.runner.DynamoDBConfig
import ar.edu.unq.eperdemic.utils.DataServiceImpl
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.Projection
import com.amazonaws.services.dynamodbv2.model.ProjectionType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.util.TableUtils
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FeedDynamoTest {

    private lateinit var dynamoDAO: DynamoFeedDAO
    private lateinit var feedService: FeedService
    private val dynamoDBConfig: DynamoDBConfig = DynamoDBConfig()
    private val dynamoDBClient: DynamoDB = DynamoDB(dynamoDBConfig.amazonDynamoDB())

    private lateinit var dataService: DataServiceImpl

    lateinit var mapper: DynamoDBMapper

    @Before
    fun prepare() {
        this.dataService = DataServiceImpl()
        this.dataService.crearSetDatosIniciales()
        dynamoDAO = DynamoFeedDAO()
        feedService = FeedServiceImpl()
        mapper = DynamoDBMapper(dynamoDBConfig.amazonDynamoDB())
        val provisionedThroughput = ProvisionedThroughput(5L, 1L)
        val ctr = mapper.generateCreateTableRequest(Evento::class.java)
                .withProvisionedThroughput(provisionedThroughput)
        ctr.globalSecondaryIndexes.forEach {
                                            gsi ->
                                                    gsi.provisionedThroughput = provisionedThroughput
                                                    gsi.projection = Projection().withProjectionType(ProjectionType.ALL)
                                            }
        TableUtils.createTableIfNotExists(dynamoDBConfig.amazonDynamoDB(), ctr)
        TableUtils.waitUntilActive(dynamoDBConfig.amazonDynamoDB(), ctr.tableName)
    }

    @Test
    fun testFeedUbicacionSinEventos() {
        val result = feedService.feedUbicacion("test")
        Assert.assertEquals(0, result.size)
    }

    @Test
    fun testFeedUbicacionConEventos() {
        val ubiTest = Ubicacion("test")
        val patogeno = Patogeno()
        val especie = Especie()
        especie.patogeno = patogeno
        dynamoDAO.save(Evento().eventoVectorViajes(Vector(), ubiTest))
        Assert.assertEquals(1, feedService.feedUbicacion("test").size)
        dynamoDAO.save(Evento().eventoEspecieEnNuevaUbicacion(especie, ubiTest))
        Assert.assertEquals(2, feedService.feedUbicacion("test").size)
    }

    @Test
    fun `test feedVector - Evento Arribo - vector se mueve de una ubicacion a otra y genera un arribo`() {
        dataService.ubicacionService.conectar("Ubicacion1","Ubicacion4","Terrestre")
        dataService.ubicacionService.mover(1, "Ubicacion4")
        Assert.assertEquals(1,feedService.feedVector(1).size)
    }

    @Test
    fun `test feedVector - Evento Contagio - vector contagia a otro con un patogeno`() {
        val vAnimal = dataService.vectorService.recuperarVector(2)
        val vHumano = dataService.vectorService.recuperarVector(4)
        vHumano.horizonteDeContagio = 0
        vHumano.infecciones = hashSetOf()
        dataService.vectorService.contagiar(vAnimal, listOf(vHumano))

        var eventos = feedService.feedVector(4)

        Assert.assertEquals(1, vHumano.infecciones.size)

        Assert.assertTrue(eventos.any {
            it.log.contains("El Vector: ${vHumano.id} esta infectado", ignoreCase = true)
        })

        eventos = feedService.feedVector(2)

        Assert.assertTrue(eventos.any {
            it.log.contains("El Vector: ${vAnimal.id} ha infectado al vector ${vHumano.id}", ignoreCase = true)
        })
    }

    @Test
    fun `test feedPatogeno - Evento Mutacion al crearse una nueva especie del patogeno`() {
        val patogeno = Patogeno("Coronavirus")
        val patogenoId = dataService.patogenoService.crearPatogeno(patogeno)
        dataService.patogenoService.agregarEspecie(patogenoId, "Sars-Cov-2", "China")
        // crearSetDatosIniciales() crea 4 especies del Patogeno1
        Assert.assertEquals(1, feedService.feedPatogeno("Coronavirus").size)
    }

    @Test
    fun `test feedPatogeno - Evento Mutacion cuando una especie muta`() {
        val patogeno = Patogeno("Coronavirus")
        val patogenoId = dataService.patogenoService.crearPatogeno(patogeno)
        var mutacion0 = dataService.mutacionService.crearMutacion(Mutacion(0,0,0,0))
        var especieCovid = dataService.patogenoService.agregarEspecie(patogenoId, "Sars-Cov-2", "China")
        especieCovid.mutacionesDisponibles.add(mutacion0)
        dataService.patogenoService.actualizarEspecie(especieCovid)

        dataService.mutacionService.mutar(especieCovid.id!!, mutacion0.id!!)
        val eventos = feedService.feedPatogeno("Coronavirus")
        Assert.assertTrue(eventos.any {
            it.log.contains("La Especie: ${especieCovid.nombre} perteneciento al Patogeno: ${patogeno.tipo} ha mutado", ignoreCase = true)
        })
    }

    @After
    fun closeTests() {
        val table = dynamoDBClient.getTable("Evento")
        table.delete()
        table.waitForDelete()
        dataService.deleteAll()
    }
}