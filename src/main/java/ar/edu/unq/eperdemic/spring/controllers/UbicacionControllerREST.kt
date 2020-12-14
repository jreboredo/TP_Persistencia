package ar.edu.unq.eperdemic.spring.controllers

import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.spring.controllers.dto.UbicacionDTO
import org.springframework.web.bind.annotation.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@CrossOrigin
@ServiceREST
@RequestMapping("/ubicacion")
class UbicacionControllerREST(private val ubicacionService: UbicacionService) {

  @PutMapping("/{vectorId}/{nombreDeLaUbicacion}")
  fun mover(@PathVariable vectorId: Int, @PathVariable nombreDeLaUbicacion: String ) = ubicacionService.mover(vectorId, nombreDeLaUbicacion)

  @PutMapping("/expandir/{nombreDeLaUbicacion}")
  fun expandir(@PathVariable nombreDeLaUbicacion: String) = ubicacionService.expandir(nombreDeLaUbicacion)

  @PostMapping
  fun crearUbicacion(@RequestBody ubicacionDTO: UbicacionDTO) = ubicacionService.crearUbicacion(ubicacionDTO.nombreDeLaUbicacion)

}