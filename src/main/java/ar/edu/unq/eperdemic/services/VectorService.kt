package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector

interface VectorService {

    /* Operaciones CRUD */
    fun crearVector(vector: Vector): Int
    fun recuperarVector(vectorId: Int): Vector
    fun borrarVector(vectorId: Int)
    fun actualizar(vector: Vector)
    fun actualizarNotransaccional(vector: Vector)
    fun recuperarATodos(): List<Vector>
    fun recuperarPorUbicacion(ubicacion: Ubicacion): List<Vector>
    fun recuperarUbicacionesDeVectoresInfectadosCon(especie: Especie): List<Ubicacion>
    fun contagiar(vectorInfectado: Vector, vectores: List<Vector>)
    fun infectar(vector: Vector, especie: Especie)

    fun moverYContagiar(vectorInfectado: Vector, vectores: List<Vector>)

    fun enfermedades(vectorId: Int): List<Especie>

}