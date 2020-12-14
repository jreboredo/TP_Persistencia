package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno

interface PatogenoService {
    fun crearPatogeno(patogeno: Patogeno): Int
    fun recuperarPatogeno(id: Int): Patogeno
    fun recuperarATodosLosPatogenos(): List<Patogeno>
    fun agregarEspecie(id: Int, nombreEspecie: String, paisDeOrigen : String) : Especie
    fun actualizar(patogeno: Patogeno)
    fun cantidadDeInfectados (especieId: Int) : Int
    fun esPandemia (especieId: Int) : Boolean
    fun recuperarEspecie(id: Int): Especie
    fun actualizarEspecie(especie: Especie)
}