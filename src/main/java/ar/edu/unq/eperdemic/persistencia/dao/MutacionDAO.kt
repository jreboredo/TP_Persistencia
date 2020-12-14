package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Mutacion

interface MutacionDAO {
    fun guardar(mutacion: Mutacion)
    fun recuperar(idMutacion: Int): Mutacion
    fun actualizar(mutacion: Mutacion)
    fun recuperarATodos() : List<Mutacion>
}