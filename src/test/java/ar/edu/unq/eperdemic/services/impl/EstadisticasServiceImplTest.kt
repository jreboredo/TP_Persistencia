package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.VectorType
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.utils.DataServiceImpl
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EstadisticasServiceImplTest {
    lateinit var dataDao: HibernateDataDAO
    lateinit var dataService: DataServiceImpl

    @Before
    fun prepare() {
        this.dataService = DataServiceImpl()
        this.dataDao = HibernateDataDAO()
        this.dataService.crearSetDatosIniciales()
    }

    @Test
    fun testTengoEspecieLiderYLaObtengo(){
        var lider = this.dataService.estadisticasService2.especieLider()
        Assert.assertEquals("Especie4", lider.nombre)
        var ubicacion6 = dataService.ubicacionService.crearUbicacion("Ubicacion6")
        var v6 = Vector(VectorType.Humano, ubicacion6)
        dataService.vectorService.crearVector(v6)
        var v7 = Vector(VectorType.Humano, ubicacion6)
        dataService.vectorService.crearVector(v7)
        var v8 = Vector(VectorType.Humano, ubicacion6)
        dataService.vectorService.crearVector(v8)
        var e3 = runTrx{dataService.especieDAO.recuperar(3)}
        dataService.vectorService.infectar(v6,e3)
        dataService.vectorService.infectar(v7,e3)
        dataService.vectorService.infectar(v8,e3)
        var newLider = this.dataService.estadisticasService2.especieLider()
        Assert.assertEquals("Especie3", newLider.nombre)
    }

    @Test
    fun EspeciesLideres(){
        var ubicacion6 = dataService.ubicacionService.crearUbicacion("Ubicacion6")
        var v6 = Vector(VectorType.Humano, ubicacion6)
        dataService.vectorService.crearVector(v6)
        var e3 = runTrx { dataService.especieDAO.recuperar(3) }
        dataService.vectorService.infectar(v6,e3)
        var newLideres = this.dataService.estadisticasService2.lideres()
        Assert.assertEquals(4, newLideres.size)
    }

    @Test
    fun testReciboUnReporteDeContagiosValido(){
        var report = this.dataService.estadisticasService2.reporteDeContagios("Ubicacion1")
        Assert.assertEquals("Especie1", report.nombreDeEspecieMasInfecciosa)
        Assert.assertEquals(1, report.vectoresInfectados)
        Assert.assertEquals(1,report.vectoresPresentes)
    }

    @After
    fun closeTests() {
        TransactionRunner.runTrx { dataDao.clear() }
    }
}