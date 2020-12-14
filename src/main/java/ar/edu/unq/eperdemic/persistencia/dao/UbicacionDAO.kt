package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector

interface UbicacionDAO {
    fun guardar(ubicacion : Ubicacion)
    fun recuperar(nombreDeUbicacion : String) : Ubicacion
    fun recuperarATodos(): List<Ubicacion>
}