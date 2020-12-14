package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Patogeno

interface PatogenoDAO {
    fun guardar(patogeno: Patogeno)
    fun actualizar(patogeno: Patogeno)
    fun recuperar(idDelPatogeno: Int): Patogeno
    fun recuperarATodos() : List<Patogeno>

}