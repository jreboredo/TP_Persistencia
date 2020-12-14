package ar.edu.unq.eperdemic.spring.controllers

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.spring.controllers.dto.EspecieDTO
import ar.edu.unq.eperdemic.spring.controllers.dto.PatogenoDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@CrossOrigin
@ServiceREST
@RequestMapping("/patogeno")
class PatogenoControllerREST(private val patogenoService: PatogenoService) {

  @PostMapping
  fun create(@RequestBody patogeno: Patogeno): ResponseEntity<Patogeno> {
    val patogenoId = patogenoService.crearPatogeno(patogeno)
    return ResponseEntity(patogenoService.recuperarPatogeno(patogenoId), HttpStatus.CREATED)
  }

  @PostMapping("/{id}")
  fun agregarEspecie(@PathVariable id: Int, @RequestBody especieDTO: EspecieDTO): ResponseEntity<EspecieDTO> {
    val especie = especieDTO.nombre?.let { especieDTO.paisDeOrigen?.let { it1 -> patogenoService.agregarEspecie(id, it, it1) } }
    val dto = especie?.let { EspecieDTO.from(it) }
    return ResponseEntity(dto, HttpStatus.OK)
  }

  @GetMapping("/{id}")
  fun findById(@PathVariable id: Int) = patogenoService.recuperarPatogeno(id)

  @GetMapping
  fun getAll() = patogenoService.recuperarATodosLosPatogenos()

  @GetMapping("/infectados/{id}")
  fun getCantidadInfectados(@PathVariable id: Int) = patogenoService.cantidadDeInfectados(id)

  @GetMapping("/esPandemia/{id}")
  fun esPandemia(@PathVariable id: Int) = patogenoService.esPandemia(id)

}