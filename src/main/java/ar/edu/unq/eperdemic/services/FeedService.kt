package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.evento.Evento

interface FeedService {
    fun feedPatogeno(tipoDePatogeno: String) : List<Evento>
    fun feedVector(vectorId: Long) : List<Evento>
    fun feedUbicacion(nombreDeUbicacion: String) : List<Evento>
}