package ar.edu.unq.eperdemic.modelo

import ar.edu.unq.eperdemic.excepciones.MutacionException
import ar.edu.unq.eperdemic.utils.addWithMax
import ar.edu.unq.eperdemic.utils.subWithMin
import javax.persistence.GenerationType
import javax.persistence.*

// Son valores bastante específicos de la especie, no son constantes que tengan que usarse en varias clases de momento
private const val MAX_CAPACIDAD_CONTAGIO = 100
private const val MIN_CAPACIDAD_CONTAGIO = 0
private const val MAX_CAPACIDAD_DEFENSIVA = 100
private const val MIN_CAPACIDAD_DEFENSIVA = 0
private const val MAX_LETALIDAD = 100
private const val MIN_LETALIDAD = 0

private const val CAPACIDAD_CONTAGIO_INICIAL = 0
private const val CAPACIDAD_DEFENSIVA_INICIAL = 0
private const val LETALIDAD_INICIAL = 0

@Entity
class Especie() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    var nombre: String? = null

    var paisDeOrigen: String? = null

    var adnDisponible: Int = 0

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var patogeno: Patogeno? = null

    @JoinTable(name = "mutaciones_desbloqueadas")
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var mutacionesDesbloqueadas: MutableSet<Mutacion> = HashSet()

    @JoinTable(name = "mutaciones_disponibles")
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var mutacionesDisponibles: MutableSet<Mutacion> = HashSet()

    var esPandemia: Boolean = false

    var porcentajeDeContagioExitoso: Int = (1..100).random()

    var capacidadDeContagio: Int = CAPACIDAD_CONTAGIO_INICIAL
    var capacidadDefensiva: Int = CAPACIDAD_DEFENSIVA_INICIAL
    var letalidad: Int = LETALIDAD_INICIAL
    var cantidadDeContagios: Int = 0

    constructor(patogeno: Patogeno, nombre: String, paisDeOrigen: String):this(){
        this.nombre = nombre
        this.paisDeOrigen = paisDeOrigen
        this.patogeno = patogeno
    }

    fun nuevoContagio() {
        this.cantidadDeContagios++
    }

    fun addCapacidadDeDontagio(int: Int){
        this.capacidadDeContagio = addWithMax(this.capacidadDeContagio, int, MAX_CAPACIDAD_CONTAGIO)
    }

    fun removeCapacidadDeContagio(int: Int){
        this.capacidadDeContagio = subWithMin(this.capacidadDeContagio, int, MIN_CAPACIDAD_CONTAGIO)
    }

    fun addCapacidadDefensiva(int: Int){
        this.capacidadDefensiva = addWithMax(this.capacidadDefensiva, int, MAX_CAPACIDAD_DEFENSIVA)
    }

    fun removeCapacidadDefensiva(int: Int){
        this.capacidadDefensiva = subWithMin(this.capacidadDefensiva, int, MIN_CAPACIDAD_DEFENSIVA)
    }

    fun addLetalidad(int: Int){
        this.letalidad = addWithMax(this.letalidad, int, MAX_LETALIDAD)
    }

    fun removeLetalidad(int: Int){
        this.letalidad = subWithMin(this.letalidad, int, MIN_LETALIDAD)
    }

    fun desbloquearMutacion(mutacion: Mutacion) {
        if (puedeMutar(mutacion)){
            this.adnDisponible = this.adnDisponible.minus(mutacion.adnRequerido!!)
            this.mutacionesDesbloqueadas.add(mutacion)
            this.mutacionesDisponibles.remove(mutacion)
            this.mutacionesDisponibles.addAll(mutacion.mutacionesQueDesbloquea!!)
            agregarStats(mutacion)
        }
        else{
            throw MutacionException("No es posible esta mutacion")
        }
    }

    private fun agregarStats(mutacion: Mutacion) {
        addCapacidadDeDontagio(mutacion.capacidadDeContagio!!)
        addCapacidadDefensiva(mutacion.capacidadDefensiva!!)
        addLetalidad(mutacion.letalidad!!)
    }

    fun puedeMutar(mutacion: Mutacion): Boolean{
        return (this.adnDisponible!! >= mutacion.adnRequerido!!
                && mutacionesDesbloqueadas.containsAll(mutacion.mutacionesRequeridas!!)
                && mutacionesDisponibles.contains(mutacion))
    }

    fun puedeContagiarA(aVector: Vector): Boolean {
        return (this.porcentajeDeContagioExitoso +
                aVector.factorContagio(aVector, this.patogeno!!)) > aVector.horizonteDeContagio
    }
}