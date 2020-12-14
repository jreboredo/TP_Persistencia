package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.excepciones.DuplicatedTypeException
import ar.edu.unq.eperdemic.excepciones.NotFoundException
import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongodb.MongoFeedDAO
import ar.edu.unq.eperdemic.services.MutacionService
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.utils.DataServiceImpl
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions

class PatogenoServiceImplTest {

    lateinit var patogenoService: PatogenoService
    lateinit var patogeno: Patogeno
    lateinit var dataDao: HibernateDataDAO
    lateinit var especies: ArrayList<Especie>
    lateinit var especieDAO: EspecieDAO
    lateinit var mutacion: Mutacion
    lateinit var mutacion2: Mutacion
    lateinit var mutacionService: MutacionService
    lateinit var ubicacionService: UbicacionService
    lateinit var vectorService: VectorService
    lateinit var vectorDAO: VectorDAO
    lateinit var dataService: DataServiceImpl
    val mongoDAO = MongoFeedDAO();

    @Before
    fun prepare() {
        this.dataService = DataServiceImpl()
        this.vectorDAO = HibernateVectorDAO()
        this.vectorService = dataService.vectorService
        this.ubicacionService = dataService.ubicacionService
        this.patogenoService = dataService.patogenoService
        this.dataDao = HibernateDataDAO()
        this.patogeno = Patogeno("test")
        this.especies = ArrayList()
        this.dataDao = HibernateDataDAO()
        this.especieDAO = HibernateEspecieDAO()
        this.mutacion = Mutacion(20,0,0,0)
        this.mutacion2 = Mutacion(1,0,0,0)
        this.mutacionService = MutacionServiceImpl(HibernateMutacionDAO(), HibernateEspecieDAO())
        this.dataService.crearSetDatosIniciales()
    }

    @Test
    fun testCrearPatogeno() {
        patogenoService.crearPatogeno(patogeno)
        Assert.assertNotNull(patogeno.id)
    }

    @Test
    fun testCrearPatogenoTipoRepetido() {
        Assertions.assertThrows(DuplicatedTypeException::class.java) {
            patogenoService.crearPatogeno(patogeno)
            patogenoService.crearPatogeno(Patogeno("test")) //Virus = repetido.
        }
    }

    @Test
    fun testActualizarPatogeno(){
        var id = patogenoService.crearPatogeno(patogeno)
        patogeno.tipo = "aynose"
        patogenoService.actualizar(patogeno)
        Assert.assertEquals(patogenoService.recuperarPatogeno(id).tipo,"aynose")
    }

    @Test
    fun `recuperarPatogeno(id valido)`() {
        var patogeno  = patogenoService.crearPatogeno(patogeno)
        val patogenoRec = patogenoService.recuperarPatogeno(patogeno)
        Assert.assertEquals(patogenoRec.id, patogenoRec.id)
        Assert.assertEquals("test", patogenoRec.tipo)
    }

    @Test
    fun `recuperarPatogeno(id inexistente) throws NotFoundException`() {
        Assertions.assertThrows(NotFoundException::class.java) {
            patogenoService.recuperarPatogeno(777)
        }
    }

    @Test
    fun recuperarATodosLosPatogenos() {
        patogenoService.crearPatogeno(Patogeno("test1"))
        patogenoService.crearPatogeno(Patogeno("test2"))
        patogenoService.crearPatogeno(Patogeno("test3"))
        Assert.assertEquals(4, patogenoService.recuperarATodosLosPatogenos().size)
    }


    @Test
    fun agregarEspecie() {
        patogenoService.crearPatogeno(patogeno)
        var patogenoRecuperado = patogenoService.recuperarPatogeno(1)
        var nuevaEspecie = patogenoService.agregarEspecie(patogenoRecuperado.id!!, "EspecieTest", "Argentina")
        var especieId = nuevaEspecie.id!!
        var especieRecuperada = patogenoService.recuperarEspecie(especieId)

        val eventos = mongoDAO.getEventosEspecie(patogenoRecuperado.tipo!!)
        Assert.assertTrue(eventos.any {it -> it.log!!.contains(patogenoRecuperado.tipo!!, ignoreCase = true) })
        Assert.assertEquals(patogenoRecuperado.id, especieRecuperada.patogeno!!.id)
        Assert.assertEquals("EspecieTest", especieRecuperada.nombre)
        Assert.assertEquals("Argentina", especieRecuperada.paisDeOrigen)
    }

    @Test
    fun testRecuperarTodasLasEspecies(){
        patogenoService.crearPatogeno(patogeno)
        patogenoService.agregarEspecie(patogeno.id!!, "test1", "Argentina")
        patogenoService.agregarEspecie(patogeno.id!!, "test2", "Argentina")
        val patogenorec = patogenoService.recuperarPatogeno(patogeno.id!!)
        Assert.assertEquals(patogenorec.especies.size, 2)
    }

    @Test
    fun recuperarEspecie() {
        patogenoService.crearPatogeno(patogeno)
        var patogenoRecuperado = patogenoService.recuperarPatogeno(1)
        var nuevaEspecie = patogenoService.agregarEspecie(patogenoRecuperado.id!!, "EspecieTest", "Argentina")
        var especieId = nuevaEspecie.id!!
        var especieRecuperada = patogenoService.recuperarEspecie(especieId)
        Assert.assertEquals(patogenoRecuperado.id, especieRecuperada.patogeno!!.id)
        Assert.assertEquals("EspecieTest", especieRecuperada.nombre)
        Assert.assertEquals("Argentina", especieRecuperada.paisDeOrigen)
    }

    @Test
    fun testEspecieTieneMutacionesRequeridasYDesbloqueadas(){
        var idPat =patogenoService.crearPatogeno(patogeno)
        var nuevaEspecie = patogenoService.agregarEspecie(idPat, "EspecieTest", "Argentina")
        mutacion = mutacionService.crearMutacion(mutacion)
        mutacion2 = mutacionService.crearMutacion(mutacion2)
        nuevaEspecie.mutacionesDisponibles.add(mutacion)
        nuevaEspecie.mutacionesDesbloqueadas.add(mutacion2)
        runTrx { especieDAO.actualizar(nuevaEspecie) }
        var especieRecuperada = patogenoService.recuperarEspecie(nuevaEspecie.id!!)
        Assert.assertEquals(1, especieRecuperada.mutacionesDesbloqueadas.size)
        Assert.assertEquals(1, especieRecuperada.mutacionesDisponibles.size)
    }

    @Test
    fun `es pandemia con mas de la mitad de locaciones disponibles`() {
        runTrx { dataDao.clear() }
        var patogeno = Patogeno("test")
        var especie = Especie(patogeno,"EspeciePandemia","UnPais")
        var ubicacion = Ubicacion("PandemiaTest")
        var vector = Vector(VectorType.Humano, ubicacion)
        vector.infecciones.add(especie)
        runTrx { especieDAO.guardar(especie) }
        vectorService.crearVector(vector)

        Assertions.assertTrue(runTrx { patogenoService.esPandemia(especie.id!!) })

    }

    @Test
    fun `no es pandemia con menos de la mitad de locaciones disponibles`() {
        var patogeno = Patogeno("test")
        var especie = Especie(patogeno,"EspeciePandemia","UnPais")
        var ubicacion = Ubicacion("PandemiaTest")
        var vector = Vector(VectorType.Humano, ubicacion)
        vector.infecciones.add(especie)
        runTrx { especieDAO.guardar(especie)
            vectorDAO.guardar(vector)}


        Assertions.assertFalse(runTrx { patogenoService.esPandemia(especie.id!!) })

    }


    @After
    fun closeTests() {
        runTrx {dataDao.clear()}
    }
}