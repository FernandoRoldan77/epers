package ar.edu.unq.eperdemic.spring.controllers

import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.services.interfaces.PatogenoService
import ar.edu.unq.eperdemic.spring.controllers.dto.EspecieDTO
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
    return ResponseEntity(patogenoService.recuperarPatogenoId(patogenoId), HttpStatus.CREATED)
  }

  @PostMapping("/{id}")
  fun agregarEspecie(@PathVariable id: Int, @RequestBody especieDTO: EspecieDTO): ResponseEntity<EspecieDTO> {
    val especie = patogenoService.agregarEspecie(id, especieDTO.nombre, especieDTO.paisDeOrigen)
    val dto = EspecieDTO.from(especie)
    return ResponseEntity(dto, HttpStatus.OK)
  }

  @GetMapping("/{id}")
  fun findById(@PathVariable id: Int) = patogenoService.recuperarPatogenoId(id)

  @GetMapping
  fun getAll() = patogenoService.recuperarTodosLosPatogenos()

  @GetMapping("/infectados/{id}")
  fun getCantidadInfectados(@PathVariable id: Int) = patogenoService.cantidadDeInfectados(id)

  @GetMapping("/esPandemia/{id}")
  fun esPandemia(@PathVariable id: Int) = patogenoService.esPandemia(id)

}