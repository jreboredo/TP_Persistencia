package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.excepciones.MutacionException
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.MutacionService
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.utils.DataServiceImpl
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions

internal class MutacionServiceImplTest() {

    lateinit var dataDao: DataDAO
    lateinit var mutacionService: MutacionService
    lateinit var patogenoService: PatogenoService
    lateinit var mutacion: Mutacion
    lateinit var especie: Especie
    lateinit var patogeno: Patogeno
    lateinit var mutacion2: Mutacion
    lateinit var dataService: DataServiceImpl
    lateinit var especieDAO: EspecieDAO

    @Before
    fun prepare() {

        this.dataDao = HibernateDataDAO()
        this.dataService = DataServiceImpl()
        dataService.crearSetDatosIniciales()
        this.patogenoService = dataService.patogenoService
        this.mutacionService = MutacionServiceImpl(HibernateMutacionDAO(), HibernateEspecieDAO())
        this.mutacion = Mutacion(20,0,0,0)
        this.mutacion2 = Mutacion(1,0,0,0)
        this.patogeno = Patogeno("test")
        this.especie = Especie(patogeno, "covic-92", "China")
        this.especieDAO = dataService.especieDAO

    }

    @Test
    fun testAgregarMutacion(){
        mutacionService.crearMutacion(mutacion)
        Assert.assertNotNull(mutacion.id)
    }

    @Test
    fun testRecuperarMutacion(){
        val mutacion = mutacionService.crearMutacion(mutacion)
        val mutacionRec = mutacionService.recuperarMutacion(mutacion.id!!)
        Assert.assertEquals(mutacion.id, mutacionRec.id)
    }

    @Test
    fun testMutacionTieneMutacionesRequeridas(){
        mutacion.mutacionesRequeridas!!.add(mutacion2)
        mutacionService.crearMutacion(mutacion)
        Assert.assertNotNull(mutacion2.id)
        mutacion = mutacionService.recuperarMutacion(mutacion.id!!)
        Assert.assertEquals(1, mutacion.mutacionesRequeridas!!.size)
    }

    @Test
    fun testMutacionTieneMutacionesQueDesbloquea(){
        mutacion.mutacionesQueDesbloquea!!.add(mutacion2)
        mutacionService.crearMutacion(mutacion)
        Assert.assertNotNull(mutacion2.id)
        mutacion = mutacionService.recuperarMutacion(mutacion.id!!)
        Assert.assertEquals(1, mutacion.mutacionesQueDesbloquea!!.size)
    }

    @Test
    fun `especie tiene 30 de adn e intenta con una mutacion que cuesta 20`() {
        val pat2 = patogenoService.crearPatogeno(patogeno)
        var mutacion = mutacionService.crearMutacion(mutacion)
        var nuevaEspecie = patogenoService.agregarEspecie(pat2, "EspecieTest", "Argentina")
        var especieRecuperada = patogenoService.recuperarEspecie(nuevaEspecie.id!!)
        especieRecuperada.mutacionesDisponibles.add(mutacion)
        especieRecuperada.adnDisponible = 30
        patogenoService.actualizarEspecie(especieRecuperada)
        mutacionService.mutar(especieRecuperada.id!!,mutacion.id!!)
        especieRecuperada = patogenoService.recuperarEspecie(especieRecuperada.id!!)
        Assert.assertTrue(especieRecuperada.mutacionesDesbloqueadas.contains(mutacion))
    }

    @Test
    fun testDesbloquearMutacionDesbloqueaNuevasMutaciones(){
        val pat2 = patogenoService.crearPatogeno(patogeno)
        var mutacion2 = Mutacion(1,10,10,10)
        mutacion.mutacionesQueDesbloquea!!.add(mutacion2)
        var nuevaEspecie = patogenoService.agregarEspecie(pat2, "EspecieTest", "Argentina")
        var mutacion = mutacionService.crearMutacion(mutacion)
        nuevaEspecie.adnDisponible = 20
        nuevaEspecie.mutacionesDisponibles.add(mutacion)
        patogenoService.actualizarEspecie(nuevaEspecie)
        mutacionService.mutar(nuevaEspecie.id!!,mutacion.id!!)
        var especieRecuperada = patogenoService.recuperarEspecie(nuevaEspecie.id!!)
        Assert.assertEquals(1, especieRecuperada.mutacionesDisponibles.size)


    }

    @Test
    fun `mutacion incrementa valores de atributo de una especie`() {
        var patogenoAtt = Patogeno("Valores")
        var especieAtt = Especie(patogenoAtt,"especieAtt","Lugar")
        var mutacion = Mutacion(5,5,5,5)
        especieAtt.mutacionesDisponibles.add(mutacion)
        especieAtt.adnDisponible = 20
        runTrx { especieDAO.guardar(especieAtt) }
        mutacionService.mutar(especieAtt.id!!,mutacion.id!!)
        var especieMutada = patogenoService.recuperarEspecie(especieAtt.id!!)
        Assert.assertEquals(especieMutada.adnDisponible,15)
        Assert.assertEquals(especieMutada.capacidadDeContagio,5)
        Assert.assertEquals(especieMutada.capacidadDefensiva,5)
        Assert.assertEquals(especieMutada.letalidad,5)
    }


    @Test
    fun `especie NO tiene suficiente ADN para crear mutacion`() {
        var especie = patogenoService.recuperarEspecie(1)
        var mutacion = mutacionService.recuperarMutacion(1)
        especie.adnDisponible = 0
        mutacion.adnRequerido = 1
        Assertions.assertThrows(MutacionException::class.java){
            especie.desbloquearMutacion(mutacion)
        }

    }


    @Test
    fun `especie no tiene mutaciones correlativas con otra mutacion y NO la puede crear`() {
        var especie = patogenoService.recuperarEspecie(1)
        var mutacion = mutacionService.recuperarMutacion(1)
        especie.mutacionesDisponibles.remove(mutacion)
        Assertions.assertThrows(MutacionException::class.java){
            especie.desbloquearMutacion(mutacion)
        }

    }

    @After
    fun closeTests() {
        runTrx { dataDao.clear() }
    }
}