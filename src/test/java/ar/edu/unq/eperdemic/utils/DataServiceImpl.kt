package ar.edu.unq.eperdemic.utils

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongodb.MongoFeedDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.Neo4JDataDAO
import ar.edu.unq.eperdemic.services.EstadisticasService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx


class DataServiceImpl : DataService {

    var especieDAO: EspecieDAO = HibernateEspecieDAO()
    var vectorDAO: VectorDAO = HibernateVectorDAO()
    var mutacionDAO: MutacionDAO = HibernateMutacionDAO()
    var patogenoService: PatogenoServiceImpl = PatogenoServiceImpl(HibernatePatogenoDAO(), especieDAO, HibernateUbicacionDAO(), HibernateVectorDAO())
    var vectorService: VectorService = VectorServiceImpl(vectorDAO, especieDAO,patogenoService)
    var ubicacionService: UbicacionService = UbicacionServiceImpl(HibernateUbicacionDAO(), vectorService)
    var estadisticasService2: EstadisticasService = EstadisticasServiceImpl(vectorDAO, especieDAO,HibernateUbicacionDAO())
    var mutacionService: MutacionServiceImpl = MutacionServiceImpl(mutacionDAO, especieDAO)

    private val dataDAO = HibernateDataDAO()
    private val neo4JDataDAO = Neo4JDataDAO()

    override fun crearSetDatosIniciales() {


        ubicacionService.crearUbicacion("Ubicacion3")
        ubicacionService.crearUbicacion("Ubicacion4")
        ubicacionService.crearUbicacion("Ubicacion5")

        this.crearDatosPatogenoYEspecie()

    }

    fun crearDatosPatogenoYEspecie(){
        var ubicacion1 = ubicacionService.crearUbicacion("Ubicacion1")
        var ubicacion2 = ubicacionService.crearUbicacion("Ubicacion2")
        var mutacion1 = Mutacion(5,5,5,5)
        var mutacion2 = Mutacion(20,5,5,5)
        var mutacion3 = Mutacion(20,100,100,100)
        var patogeno = Patogeno("Patogeno1")
        var especies = HashSet<Especie>()
        var especie1 = Especie(patogeno,"Especie1","Pais1")
        var especie2 = Especie(patogeno,"Especie2","Pais2")
        var especie3 = Especie(patogeno,"Especie3","Pais3")
        var especie4 = Especie(patogeno,"Especie4","Pais4")
        var vector1 = Vector(VectorType.Animal, ubicacion1)
        var vector2 = Vector(VectorType.Animal, ubicacion2)
        var vector3 = Vector(VectorType.Animal, ubicacion2)
        var vector4 = Vector(VectorType.Humano, ubicacion2)
        var vector5 = Vector(VectorType.Animal, ubicacion2)
        var vector6 = Vector(VectorType.Humano, Ubicacion("Ubicacion3"))
        vector1.infecciones.add(especie1)
        vector2.infecciones.add(especie2)
        vector3.infecciones.add(especie3)
        vector4.infecciones.add(especie4)
        especie1.adnDisponible = 50
        especie2.adnDisponible = 10
        vector5.infecciones.add(especie1)

        mutacion1.mutacionesQueDesbloquea!!.add(mutacion2)
        mutacion2.mutacionesQueDesbloquea!!.add(mutacion3)
        mutacion2.mutacionesRequeridas!!.add(mutacion1)
        mutacion3.mutacionesRequeridas!!.add(mutacion1)
        mutacion3.mutacionesRequeridas!!.add(mutacion2)

        especie1.mutacionesDisponibles.add(mutacion1)
        especie2.mutacionesDisponibles.add(mutacion1)

        especies.add(especie1)
        especies.add(especie2)
        patogeno.agregarEspecies(especies)

        patogenoService.crearPatogeno(patogeno)
        vectorService.crearVector(vector1)
        vectorService.crearVector(vector2)
        vectorService.crearVector(vector3)
        vectorService.crearVector(vector4)
        vectorService.crearVector(vector5)
        vectorService.crearVector(vector6)
    }

    override fun deleteAll() {
        runTrx { dataDAO.clear(); neo4JDataDAO.clear()}

    }


}