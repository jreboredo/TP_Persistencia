package ar.edu.unq.eperdemic.modelo

import javax.persistence.*

@Entity
class Mutacion() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var adnRequerido: Int? = null
    var capacidadDeContagio: Int? = null
    var capacidadDefensiva: Int? = null
    var letalidad: Int? = null

    @JoinTable(name = "mutaciones_requeridas")
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var mutacionesRequeridas: MutableSet<Mutacion>? = HashSet()

    @JoinTable(name = "mutaciones_que_desbloquea")
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var mutacionesQueDesbloquea: MutableSet<Mutacion>? = HashSet()

    constructor(adnRequerido: Int, capacidadDeContagio: Int, capacidadDefensiva: Int, letalidad: Int) : this() {
        this.adnRequerido = adnRequerido
        this.capacidadDeContagio = capacidadDeContagio
        this.capacidadDefensiva = capacidadDefensiva
        this.letalidad = letalidad
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mutacion

        if (id != other.id) return false
        if (adnRequerido != other.adnRequerido) return false
        if (mutacionesRequeridas != other.mutacionesRequeridas) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (adnRequerido ?: 0)
        result = 31 * result + (capacidadDeContagio ?: 0)
        result = 31 * result + (capacidadDefensiva ?: 0)
        result = 31 * result + (letalidad ?: 0)
        return result
    }


}