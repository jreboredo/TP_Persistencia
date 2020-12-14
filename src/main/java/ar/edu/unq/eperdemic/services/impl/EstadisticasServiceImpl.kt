package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.ReporteDeContagios
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.EstadisticasService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class EstadisticasServiceImpl(val vectorDAO: VectorDAO, val especieDAO: EspecieDAO, val ubicacionDAO: UbicacionDAO) : EstadisticasService {

    override fun especieLider(): Especie {
        return runTrx { especieDAO.especieLider() }
    }

    override fun lideres(): List<Especie> {
        return runTrx { especieDAO.especiesLideres() }
    }

    override fun reporteDeContagios(nombreUbicacion: String): ReporteDeContagios {
        lateinit var report: ReporteDeContagios
         runTrx {
             var ubicacion = ubicacionDAO.recuperar(nombreUbicacion)
             var vectoresEnUbicacion = vectorDAO.recuperarVectoresPorUbicacion(ubicacion).size
             var cantidadDeVectoresInfectados = vectorDAO.cantidadDeVectoresInfectadosEn(nombreUbicacion).size
             var especieLiderTodoTipo = especieDAO.especieLiderTodoTipo().nombre!!
             report = ReporteDeContagios(vectoresEnUbicacion, cantidadDeVectoresInfectados, especieLiderTodoTipo)
         }

        return report
    }

}