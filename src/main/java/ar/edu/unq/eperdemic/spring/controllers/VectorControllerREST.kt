package ar.edu.unq.eperdemic.spring.controllers

import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import org.springframework.web.bind.annotation.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@CrossOrigin
@ServiceREST
@RequestMapping("/vector")
class VectorControllerREST(private val vectorService: VectorService, private val patogenoService: PatogenoService) {

  @PutMapping("/infectar/{vectorId}/{especieId}")
  fun infectar(@PathVariable vectorId: Int, @PathVariable especieId: Int ){
    val vector = vectorService.recuperarVector(vectorId)
    val especie = patogenoService.recuperarEspecie(especieId)
    vectorService.infectar(vector, especie)
  }

  @GetMapping("/enfermedades/{vectorId}")
  fun enfermedades(@PathVariable vectorId: Int ) = vectorService.enfermedades(vectorId)

  @PostMapping
  fun crearVector( @RequestBody vectorFrontendDTO: VectorFrontendDTO) = vectorService.crearVector(vectorFrontendDTO.aModelo())

  @GetMapping("/{id}")
  fun recuperarVector(@PathVariable vectorId: Int) = vectorService.recuperarVector(vectorId)

  @DeleteMapping("/{id}")
  fun borrarVector(@PathVariable vectorId: Int) = vectorService.borrarVector(vectorId)

}