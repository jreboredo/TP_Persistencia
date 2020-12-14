package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.excepciones.*
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.VectorType
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.utils.DataServiceImpl
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions

class UbicacionServiceImplTest {
    lateinit var ubicacionService: UbicacionService
    lateinit var dataService : DataServiceImpl
    lateinit var ubicacion : Ubicacion

    @Before
    fun prepare() {
        this.dataService = DataServiceImpl()
        ubicacionService = dataService.ubicacionService
        dataService = DataServiceImpl()
        dataService.crearSetDatosIniciales()
    }

    @Test
    fun testCrearUbicacion() {
        ubicacion = ubicacionService.crearUbicacion("UbicacionTEST")
        Assert.assertEquals("UbicacionTEST", ubicacion.nombre)
    }

    @Test
    fun testCrearUbicacionNombreRepetido() {
        Assertions.assertThrows(DuplicatedTypeException::class.java) {
            ubicacionService.crearUbicacion("Ubicacion1") // Ubicacion1 = repetido.
        }
    }

    @Test
    fun recuperarUbicacionExistente() {
        val ubicacionRecuperada = ubicacionService.recuperarUbicacion("Ubicacion1")
        Assert.assertEquals("Ubicacion1", ubicacionRecuperada.nombre)
    }

    @Test
    fun recuperarUbicacionInexistente() {
        Assertions.assertThrows(NotFoundException::class.java) {
            ubicacionService.recuperarUbicacion("UBICACION INEXISTENTE!")
        }
    }

    @Test
    fun `mover`() {
        ubicacionService.conectar("Ubicacion1","Ubicacion4","Aereo")
        var vector = dataService.vectorService.recuperarVector(1)
        Assertions.assertEquals("Ubicacion1", vector.ubicacionActual!!.nombre)
        vector.horizonteDeContagio = 100
        dataService.vectorService.actualizar(vector)
        ubicacionService.mover(1, "Ubicacion4")
        val vectorPostMover = dataService.vectorService.recuperarVector(1)
        Assertions.assertEquals("Ubicacion4", vectorPostMover.ubicacionActual!!.nombre)
    }
    @Test
    fun `moverUbicacionMuyLejana`() {
        Assertions.assertThrows(ImposibleMoverseException::class.java) {
            ubicacionService.mover(1, "Ubicacion3")
        }
    }
    @Test
    fun `expandir`() {
        ubicacionService.expandir("Ubicacion2")
        val vector2 = runTrx { dataService.vectorDAO.recuperar(2) }
        val vector3 = runTrx { dataService.vectorDAO.recuperar(3) }
        Assertions.assertEquals(vector2.infecciones.size, vector3.infecciones.size)
    }

    @Test
    fun conectar() {
        ubicacionService.conectar("Ubicacion1", "Ubicacion2", "Aereo")
        Assert.assertEquals("Ubicacion2", ubicacionService.conectados("Ubicacion1")[0].nombre)
    }

    @Test
    fun nodoTieneDosMasConectados(){
        ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
        ubicacionService.conectar("Ubicacion1","Ubicacion3","Terrestre")
        ubicacionService.conectar("Ubicacion2","Ubicacion3","Maritimo")
        ubicacionService.conectar("Ubicacion3","Ubicacion4","Aereo")

        var result = ubicacionService.conectados("Ubicacion1")
        Assert.assertEquals("Ubicacion3", result[0].nombre)
        Assert.assertEquals("Ubicacion2", result[1].nombre)
    }

    @Test
    fun testUnVectorHumanoPuedeMoversePorUnTerrenoMaritimo(){
        var vector = Vector(VectorType.Humano, Ubicacion("Ubicacion1"))
        ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
        ubicacionService.conectar("Ubicacion1","Ubicacion3","Maritimo")
        Assertions.assertTrue(ubicacionService.puedeMover(vector,Ubicacion("Ubicacion3")))
    }

    @Test
    fun MuevoAlCaminoMasCortoVectorHumano(){
        ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
        ubicacionService.conectar("Ubicacion1","Ubicacion3","Maritimo")
        ubicacionService.moverMasCorto(1,"Ubicacion3")
        Assertions.assertEquals("Ubicacion3", dataService.vectorService.recuperarVector(1).ubicacionActual!!.nombre)
    }
    @Test
    fun MuevoAlCaminoMasCortoVectorHumanoPeroNoSeMuevePorQueEsConexionAerea(){
        ubicacionService.conectar("Ubicacion2","Ubicacion3","Aereo")
        Assertions.assertThrows(UbicacionNoAlcanzableException::class.java) {
            ubicacionService.moverMasCorto(4,"Ubicacion3")
        }
    }
    @Test
    fun MuevoAlCaminoMasCortoVectorAnimalQueSeMuevePorCualquiera(){
            ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
            ubicacionService.conectar("Ubicacion2","Ubicacion3","Aereo")
            ubicacionService.conectar("Ubicacion3","Ubicacion4","Aereo")
            ubicacionService.conectar("Ubicacion4","Ubicacion5","Aereo")
            ubicacionService.moverMasCorto(1,"Ubicacion2")
            Assertions.assertEquals("Ubicacion2", dataService.vectorService.recuperarVector(1).ubicacionActual!!.nombre)
            ubicacionService.moverMasCorto(1,"Ubicacion4")
            Assertions.assertEquals("Ubicacion4", dataService.vectorService.recuperarVector(1).ubicacionActual!!.nombre)
        }
    @Test
    fun CapacidadDeExpansionDeUnAnimalEnLaUbi1Con1Mov(){
        ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
        ubicacionService.conectar("Ubicacion2","Ubicacion3","Aereo")
        ubicacionService.conectar("Ubicacion3","Ubicacion4","Aereo")
        ubicacionService.conectar("Ubicacion4","Ubicacion5","Aereo")
        Assertions.assertEquals(1, dataService.ubicacionService.capacidadDeExpansion(1.toLong(),1))
    }
    @Test
    fun CapacidadDeExpansionDeUnAnimalEnLaUbi2Con1Mov(){
        ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
        ubicacionService.conectar("Ubicacion2","Ubicacion3","Aereo")
        ubicacionService.conectar("Ubicacion3","Ubicacion4","Aereo")
        ubicacionService.conectar("Ubicacion4","Ubicacion5","Aereo")
        Assertions.assertEquals(2, dataService.ubicacionService.capacidadDeExpansion(1.toLong(),2))
    }
    @Test
    fun CapacidadDeExpansionDeUnAnimalEnLaUbi1Con0Mov(){
        ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
        ubicacionService.conectar("Ubicacion2","Ubicacion3","Aereo")
        ubicacionService.conectar("Ubicacion3","Ubicacion4","Aereo")
        ubicacionService.conectar("Ubicacion4","Ubicacion5","Aereo")
        Assertions.assertEquals(0, dataService.ubicacionService.capacidadDeExpansion(1.toLong(),0))
    }
    @Test
    fun CapacidadDeExpansionDeUnAnimalEnLaUbi1Con2MovYmasUbicacionesDisp(){
        ubicacionService.conectar("Ubicacion1","Ubicacion2","Aereo")
        ubicacionService.conectar("Ubicacion2","Ubicacion3","Aereo")
        ubicacionService.conectar("Ubicacion2","Ubicacion4","Aereo")
        ubicacionService.conectar("Ubicacion4","Ubicacion5","Aereo")
        Assertions.assertEquals(3, dataService.ubicacionService.capacidadDeExpansion(1.toLong(),2))
    }
    @After
    fun closeTests() {
        dataService.deleteAll()
    }
}