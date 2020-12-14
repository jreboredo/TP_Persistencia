package ar.edu.unq.eperdemic.modelo

import javax.persistence.*

@Entity
class Ubicacion() {
    @Id
    var nombre: String? = null

    constructor(nombreUbicacion: String):this() {
        this.nombre = nombreUbicacion
    }

    override fun toString(): String {
        return this.nombre!!
    }
}