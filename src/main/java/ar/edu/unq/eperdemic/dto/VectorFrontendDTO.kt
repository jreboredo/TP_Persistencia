package ar.edu.unq.eperdemic.dto

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.modelo.VectorType

class VectorFrontendDTO(val tipoDeVector : VectorType,
                        val nombreDeUbicacionPresente: String) {

    fun aModelo() : Vector {
       return Vector(tipoDeVector, Ubicacion(nombreDeUbicacionPresente))
    }
}