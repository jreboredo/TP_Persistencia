package ar.edu.unq.eperdemic.modelo

import ar.edu.unq.eperdemic.excepciones.InvalidInfectionException
import javax.persistence.*

@Entity
class Vector() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column
    var tipo: VectorType? = null

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var ubicacionActual: Ubicacion? = null

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var infecciones: MutableSet<Especie> = hashSetOf()

    @Transient
    var horizonteDeContagio: Int = (1..100).random()

    constructor(tipo: VectorType, ubicacion: Ubicacion) : this() {
        this.tipo = tipo
        this.ubicacionActual = ubicacion
    }

    private fun tiposPosibles(): Set<VectorType> {
        return when(this.tipo!!) {
            VectorType.Animal -> setOf(VectorType.Insecto)
            VectorType.Insecto -> setOf(VectorType.Animal, VectorType.Humano)
            VectorType.Humano -> setOf(VectorType.Animal, VectorType.Insecto, VectorType.Humano)
        }
    }

    fun esDeTipoValidoParaInfectar(vectorInfectado: Vector): Boolean {
        // Denota si este vector puede ser infectado por otro vector ya infectado (segun tipos)
        return tiposPosibles().any { it === vectorInfectado.tipo!! }
    }

    fun factorContagio(vector: Vector, aPatogeno: Patogeno): Int {
        return when(vector.tipo!!) {
            VectorType.Animal -> aPatogeno.factorContagioAnimal
            VectorType.Insecto -> aPatogeno.factorContagioInsecto
            VectorType.Humano -> aPatogeno.factorContagioHumano
        }
    }

    fun infectadoCon(aEspecie: Especie): Boolean {
        return this.infecciones.contains(aEspecie)
    }

    fun infectar(aEspecie: Especie) {
        this.infecciones.add(aEspecie)
        aEspecie.nuevoContagio()
    }

    fun prevenirInfectarseASiMismo(vectorInfectado: Vector) {
        if(this.id == vectorInfectado.id) {
            throw InvalidInfectionException("Vector está intentando infectarse a sí mismo!")
        }
    }
}