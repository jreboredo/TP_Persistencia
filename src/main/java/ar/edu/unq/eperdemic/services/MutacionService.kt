package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Mutacion

interface MutacionService {

    fun mutar (especieId: Int, mutacionId: Int)
    /* Operaciones CRUD */
    fun crearMutacion(mutacion: Mutacion): Mutacion
    fun recuperarMutacion(mutacionId: Int): Mutacion
    fun recuperarTodas(): List<Mutacion>
}