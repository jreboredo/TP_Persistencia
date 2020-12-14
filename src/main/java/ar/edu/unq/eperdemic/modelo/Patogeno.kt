package ar.edu.unq.eperdemic.modelo

import javax.persistence.*
import kotlin.collections.HashSet

@Entity
class Patogeno() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @OneToMany(mappedBy = "patogeno", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var especies: MutableSet<Especie> = HashSet()

    @Column(unique = true, nullable = false, length = 500)
    var tipo: String? = null

    @Column
    var factorContagioAnimal: Int = 40

    @Column
    var factorContagioInsecto: Int = 25

    @Column
    var factorContagioHumano: Int = 50

    constructor(tipo: String) : this() {
        this.tipo = tipo
    }

    fun agregarEspecie(nombreEspecie : String, paisDeOrigen : String) : Especie {
        var especie = Especie(this, nombreEspecie, paisDeOrigen)
        this.especies.add(especie)
        return especie
    }

    fun agregarEspecies(especies: Set<Especie>) {
        this.especies.addAll(especies)
    }
}