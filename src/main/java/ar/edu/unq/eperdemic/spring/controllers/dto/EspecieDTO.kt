package ar.edu.unq.eperdemic.spring.controllers.dto

import ar.edu.unq.eperdemic.modelo.Especie

class EspecieDTO (val nombre : String?,
                  val paisDeOrigen: String?,
                  val patogenoId: Int?){


    companion object {
        fun from(especie:Especie) =
                EspecieDTO(especie.nombre,
                        especie.paisDeOrigen,
                        especie.patogeno?.id)
    }

}


