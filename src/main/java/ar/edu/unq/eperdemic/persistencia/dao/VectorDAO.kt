package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector

interface VectorDAO {
    fun guardar(vector: Vector)
    fun recuperar(id: Int) : Vector
    fun borrar(id: Int)
    fun actualizar(vector: Vector)
    fun recuperarATodos(): List<Vector>
    fun recuperarVectoresPorUbicacion(ubicacion: Ubicacion): List<Vector>
    fun recuperarUbicacionesDeVectoresInfectadosCon(especie: Especie): List<Ubicacion>
    fun cantidadDeVectoresInfectadosEn(nombreUbicacion: String): List<Vector>
}