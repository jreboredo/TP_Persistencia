package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion

interface EspecieDAO {
    fun guardar(especie: Especie)
    fun recuperar(idEspecie: Int): Especie
    fun actualizar(especie: Especie)
    fun especieLider(): Especie
    fun especiesLideres() : List<Especie>
    fun especieLiderTodoTipo(): Especie
    fun especieSeEncuentraEn(ubicacion: Ubicacion): Boolean
}