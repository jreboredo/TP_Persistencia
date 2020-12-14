package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.excepciones.InvalidInfectionException
import ar.edu.unq.eperdemic.excepciones.NotFoundException
import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.utils.DataServiceImpl
import junit.framework.Assert.*
import org.junit.*
import org.junit.jupiter.api.Assertions.assertThrows

internal class VectorServiceImplTest {

    lateinit var dataDao: HibernateDataDAO
    lateinit var vectorInsecto: Vector
    lateinit var vectorAnimal: Vector
    lateinit var vectorHumano: Vector
    lateinit var vectorAnimalInfectado: Vector
    lateinit var vectorHumanoInfectado: Vector
    lateinit var vectorInsectoInfectado: Vector

    lateinit var vService: VectorService
    lateinit var patogenoService: PatogenoService
    lateinit var dataService: DataServiceImpl

    lateinit var vectoresInfectados: MutableSet<Vector>

    lateinit var virus: Patogeno
    lateinit var infeccionMuyContagiosa1: Especie
    lateinit var infeccionMuyContagiosa2: Especie
    lateinit var infeccionMuyContagiosa3: Especie

    @Before
    fun prepare() {
        this.dataService = DataServiceImpl()
        this.patogenoService = dataService.patogenoService
        this.vService = dataService.vectorService
        this.dataDao = HibernateDataDAO()
        this.vectorInsecto = Vector()
        this.vectorAnimal = Vector()
        this.vectorHumano = Vector()
        this.vectorAnimalInfectado = Vector()
        this.vectorHumanoInfectado = Vector()
        this.vectorInsectoInfectado = Vector()

        var unqui = Ubicacion("Unqui")
        this.vectorHumano.ubicacionActual = unqui
        this.vectorInsecto.ubicacionActual = unqui
        this.vectorAnimal.ubicacionActual = unqui
        this.vectorAnimalInfectado.ubicacionActual = unqui
        this.vectorHumanoInfectado.ubicacionActual = unqui
        this.vectorInsectoInfectado.ubicacionActual = unqui


        this.vectorInsecto.tipo = VectorType.Insecto
        this.vectorInsectoInfectado.tipo = VectorType.Insecto
        this.vectorAnimal.tipo = VectorType.Animal
        this.vectorAnimalInfectado.tipo = VectorType.Animal
        this.vectorHumano.tipo = VectorType.Humano
        this.vectorHumanoInfectado.tipo = VectorType.Humano

        this.virus = Patogeno("virus")
        this.dataService.patogenoService.crearPatogeno(virus)

        this.infeccionMuyContagiosa1 = Especie(virus, "H1N1", "China")
        this.infeccionMuyContagiosa2 = Especie(virus, "Dengue", "China")
        this.infeccionMuyContagiosa3 = Especie(virus, "SARS-Cov-2", "China")

        this.infeccionMuyContagiosa1.porcentajeDeContagioExitoso = 200
        this.infeccionMuyContagiosa2.porcentajeDeContagioExitoso = 200
        this.infeccionMuyContagiosa3.porcentajeDeContagioExitoso = 200

        runTrx{this.dataService.especieDAO.guardar(infeccionMuyContagiosa1)}
        runTrx{this.dataService.especieDAO.guardar(infeccionMuyContagiosa2)}
        runTrx{this.dataService.especieDAO.guardar(infeccionMuyContagiosa3)}

        vService.crearVector(vectorAnimal)
        vService.crearVector(vectorHumano)
        vService.crearVector(vectorInsecto)
        vService.crearVector(vectorAnimalInfectado)
        vService.crearVector(vectorHumanoInfectado)
        vService.crearVector(vectorInsectoInfectado)


        this.vService.infectar(vectorAnimalInfectado, infeccionMuyContagiosa1)
        this.vService.infectar(vectorHumanoInfectado, infeccionMuyContagiosa2)
        this.vService.infectar(vectorInsectoInfectado, infeccionMuyContagiosa3)

        this.vectoresInfectados = mutableSetOf(vectorAnimalInfectado, vectorHumanoInfectado, vectorInsectoInfectado)


    }

    @Test
    fun `CRUD - crear vector`() {
        assertEquals(7, vService.crearVector(vectorInsecto))
    }

    @Test
    fun `CRUD - recuperar vector`() {
        assertEquals(1, vService.recuperarVector(1).id)
    }

    @Test
    fun `CRUD - recuperar vector con id inexistente throws NotFoundException`() {
        assertThrows(NotFoundException::class.java) {
            vService.recuperarVector(999)
        }
    }

    @Test
    fun `CRUD - borrar vector`() {
        vService.crearVector(Vector())
        vService.borrarVector(7)
        assertThrows(NotFoundException::class.java) {
            vService.recuperarVector(7)
        }

    }

    @Test
    fun `CRUD - borrar vector con id inexistente throws NotFoundException`() {
        assertThrows(NotFoundException::class.java) {
            vService.borrarVector(999)
        }
    }

    @Test
    fun `contagiar Animal es infectado solo por vector Insecto`() {
        vectoresInfectados.forEach {
            vService.contagiar(it, listOf(vectorAnimal))
        }
        val vector: Vector = vService.recuperarVector(vectorAnimal.id!!)

        assertEquals(1,vector.infecciones.size)
    }

    @Test
    fun `contagiar Insecto es infectado por vectores Animal y Humano`() {
        vectoresInfectados.forEach {
            vService.contagiar(it, listOf(vectorInsecto))
        }
        val vector: Vector = vService.recuperarVector(vectorInsecto.id!!)

        assertEquals(2,vector.infecciones.size)
    }

    @Test
    fun `contagiar Humano es infectado por los tres tipos de vectores`() {
        vectoresInfectados.forEach {
            vService.contagiar(it, listOf(vectorHumano))
        }
        val vector: Vector = vService.recuperarVector(vectorHumano.id!!)

        assertEquals(3,vector.infecciones.size)
    }

    @Test
    fun `contagiar Vector muy sano (horizonte de contagio alto) no infecta`() {
        vectorHumano.horizonteDeContagio = 999
        vectoresInfectados.forEach {
            vService.contagiar(it, listOf(vectorHumano))
        }
        val vector: Vector = vService.recuperarVector(vectorHumano.id!!)

        assertEquals(0,vector.infecciones.size)
    }

    @Test
    fun `Un vector no puede contagiarse a si mismo`() {
        val vectorObjetivo : List<Vector> = mutableListOf(vectorHumanoInfectado)

        assertThrows(InvalidInfectionException::class.java) {
            vService.contagiar(vectorHumanoInfectado, vectorObjetivo)
        }
    }

    @Test
    fun `infectar vector sano y consultarlo devuelve 1`() {
        val vectorId = vService.crearVector(vectorInsecto)
        vService.infectar(vectorInsecto, infeccionMuyContagiosa1)
        val vectorInfectado = vService.recuperarVector(vectorId)
        assertEquals(1,vectorInfectado.infecciones.size)
    }

    @Test
    fun `infectar un vector ya infectado con la misma especie no causa nada`() {
        val vectorId = vService.crearVector(vectorInsecto)
        vService.infectar(vectorInsecto, infeccionMuyContagiosa1)
        vService.infectar(vectorInsecto, infeccionMuyContagiosa1)

        val vectorInfectado = vService.recuperarVector(vectorId)
        assertEquals(vectorInfectado.infecciones.size, 1)
    }

    @Test
    fun `infectar un vector que no existe throws NotFoundException`() {
        val newVector = Vector()
        newVector.tipo = VectorType.Insecto
        newVector.id = 999
        assertThrows(NotFoundException::class.java) {
            vService.infectar(newVector, infeccionMuyContagiosa1)
        }
    }

    @Test
    fun `enfermedades`() {
        val vectorId = vService.crearVector(vectorInsecto)
        vService.infectar(vectorInsecto, infeccionMuyContagiosa1)
        assertEquals(infeccionMuyContagiosa1.id, vService.enfermedades(vectorId)[0].id)
    }

    @Test
    fun `pedir enfermedades de un vector que no existe throws NotFoundException`() {
        assertThrows(NotFoundException::class.java) {
            vService.enfermedades(100)
        }
    }

    @After
    fun closeTests() {
        runTrx { dataDao.clear() }
    }
}