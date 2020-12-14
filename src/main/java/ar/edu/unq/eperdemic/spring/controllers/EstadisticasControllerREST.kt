package ar.edu.unq.eperdemic.spring.controllers

import ar.edu.unq.eperdemic.services.EstadisticasService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@CrossOrigin
@ServiceREST
@RequestMapping("/estadisticas")
class EstadisticasControllerREST(private val estadisticasService: EstadisticasService) {

  @GetMapping("/especieLider")
  fun especieLider() = estadisticasService.especieLider()

  @GetMapping("/lideres")
  fun lideres() = estadisticasService.lideres()

  @GetMapping("/reporteDeContagios/{nombreDeUbicacion}")
  fun reporteDeContagios(@PathVariable nombreDeUbicacion: String) = estadisticasService.reporteDeContagios(nombreDeUbicacion)

}