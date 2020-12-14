package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.evento.EventoArribo
import ar.edu.unq.eperdemic.evento.EventoContagio
import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongodb.MongoFeedDAO
import ar.edu.unq.eperdemic.services.FeedService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.DataServiceImpl
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions

class FeedServiceImplTest {

    private lateinit var mongoDAO: MongoFeedDAO
    private lateinit var feedService: FeedService
    private lateinit var vService: VectorService
    private lateinit var dataDAO: DataDAO
    private lateinit var dataService: DataServiceImpl

    @Before
    fun prepare() {
        mongoDAO = MongoFeedDAO()
        feedService = FeedServiceImpl()
        this.dataDAO = HibernateDataDAO()
        this.dataService = DataServiceImpl()
        this.dataService.crearSetDatosIniciales()
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
        mongoDAO.save(EventoArribo().eventoVectorViajes(Vector(), ubiTest))
        Assert.assertEquals(1, feedService.feedUbicacion("test").size)
        mongoDAO.save(EventoContagio().eventoEspecieEnNuevaUbicacion(especie, ubiTest))
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
            it.log!!.contains("El Vector: ${vHumano.id} esta infectado", ignoreCase = true)
        })

        eventos = feedService.feedVector(2)

        Assert.assertTrue(eventos.any {
            it.log!!.contains("El Vector: ${vAnimal.id} ha infectado al vector ${vHumano.id}", ignoreCase = true)
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

        Assert.assertTrue(feedService.feedPatogeno("Coronavirus").any {
            it -> it.log!!.contains("La Especie: ${especieCovid.nombre} perteneciento al Patogeno: ${patogeno.tipo} ha mutado", ignoreCase = true)
        })
    }

    @Test
    fun `test feedPatogeno - Evento Contagio cuando un patogeno esPandemia`() {
        dataService.deleteAll()
        mongoDAO.deleteAll()

        val patogeno = Patogeno("Coronavirus")
        val patogenoId = dataService.patogenoService.crearPatogeno(patogeno)
        var especieCovid = dataService.patogenoService.agregarEspecie(patogenoId, "Sars-Cov-2", "China")
        var ubicacion = Ubicacion("PandemiaTest")
        var vector = Vector(VectorType.Humano, ubicacion)
        dataService.vectorService.crearVector(vector)
        vector.infectar(especieCovid)

        var vector2 = Vector(VectorType.Humano, ubicacion)
        dataService.vectorService.crearVector(vector2)
        vector2.horizonteDeContagio = 0

        dataService.vectorService.contagiar(vector, listOf(vector2))

        Assert.assertEquals(1, feedService.feedPatogeno("Coronavirus").count {
            it.log!!.contains("se ha convertido en pandemia", ignoreCase = true)
        })
    }

    @Test
    fun `test feedPatogeno - Evento Contagio cuando patogeno llega a una nueva ubicacion`() {
        dataService.deleteAll()
        mongoDAO.deleteAll()

        val patogeno = Patogeno("Coronavirus")
        val patogenoId = dataService.patogenoService.crearPatogeno(patogeno)
        var especieCovid = dataService.patogenoService.agregarEspecie(patogenoId, "Sars-Cov-2", "China")

        var ubicacion1: Ubicacion = dataService.ubicacionService.crearUbicacion("Ubicacion1")
        var ubicacion2: Ubicacion = dataService.ubicacionService.crearUbicacion("Ubicacion2")
        dataService.ubicacionService.conectar("Ubicacion1","Ubicacion2","Terrestre")

        var vector = Vector(VectorType.Humano, ubicacion1)
        dataService.vectorService.crearVector(vector)
        vector.infectar(especieCovid)

        var vector2 = Vector(VectorType.Humano, ubicacion2)
        dataService.vectorService.crearVector(vector2)
        vector2.horizonteDeContagio = 0

        var vector3 = Vector(VectorType.Humano, ubicacion2)
        dataService.vectorService.crearVector(vector3)
        vector3.horizonteDeContagio = 0

        dataService.vectorService.contagiar(vector, listOf(vector2, vector3))

        val eventos = feedService.feedPatogeno("Coronavirus")
        Assert.assertEquals(1, eventos.count {
            it.log!!.contains("ahora se encuentra tambien en: ${ubicacion2.nombre}", ignoreCase = true)
        })
    }

    @After
    fun closeTests() {
        mongoDAO.deleteAll()
        dataService.deleteAll()
    }
}