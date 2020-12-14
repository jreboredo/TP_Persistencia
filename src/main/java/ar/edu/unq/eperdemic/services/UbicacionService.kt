package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector

interface UbicacionService {


    fun mover(vectorId: Int, nombreUbicacion: String)
    fun expandir(nombreUbicacion: String)
    /* Operaciones CRUD*/
    fun crearUbicacion(nombreUbicacion: String): Ubicacion
    fun recuperarUbicacion(nombreUbicacion: String) : Ubicacion
    fun recuperarATodos(): List<Ubicacion>

    //Neo4J
    fun conectar(ubicacion1:String, ubicacion2:String, tipoCamino:String)
    fun conectados(nombreDeUbicacion:String): List<Ubicacion>
    fun puedeMover(vector: Vector, nuevaUbicacion: Ubicacion): Boolean
    fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String)
    fun capacidadDeExpansion(vectorId: Long, movimientos:Int): Int
}