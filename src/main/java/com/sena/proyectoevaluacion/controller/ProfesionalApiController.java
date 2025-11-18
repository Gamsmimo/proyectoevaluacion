package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.service.IProfesionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profesionales")
@CrossOrigin(origins = "*")
public class ProfesionalApiController {

	@Autowired
	private IProfesionalService profesionalService;

	// GET - Obtener todos los profesionales
	@GetMapping
	public ResponseEntity<List<Profesional>> obtenerTodosProfesionales() {
		try {
			List<Profesional> profesionales = profesionalService.findAll();
			return ResponseEntity.ok(profesionales);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Obtener profesional por ID
	@GetMapping("/{id}")
	public ResponseEntity<Profesional> obtenerProfesionalPorId(@PathVariable Integer id) {
		try {
			Optional<Profesional> profesional = profesionalService.findById(id);
			return profesional.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Obtener profesionales por especialidad
	@GetMapping("/especialidad/{especialidad}")
	public ResponseEntity<List<Profesional>> obtenerProfesionalesPorEspecialidad(@PathVariable String especialidad) {
		try {
			List<Profesional> profesionales = profesionalService.findByEspecialidad(especialidad);
			return ResponseEntity.ok(profesionales);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// POST - Crear nuevo profesional
	@PostMapping
	public ResponseEntity<Profesional> crearProfesional(@RequestBody Profesional profesional) {
		try {
			Profesional profesionalCreado = profesionalService.save(profesional);
			return ResponseEntity.ok(profesionalCreado);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// PUT - Actualizar profesional
	@PutMapping("/{id}")
	public ResponseEntity<Profesional> actualizarProfesional(@PathVariable Integer id,
			@RequestBody Profesional profesional) {
		try {
			Optional<Profesional> profesionalExistente = profesionalService.findById(id);
			if (profesionalExistente.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			Profesional profesionalActual = profesionalExistente.get();
			profesionalActual.setEspecialidad(profesional.getEspecialidad());
			profesionalActual.setHorarioDisponible(profesional.getHorarioDisponible());

			Profesional profesionalActualizado = profesionalService.save(profesionalActual);
			return ResponseEntity.ok(profesionalActualizado);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// DELETE - Eliminar profesional
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarProfesional(@PathVariable Integer id) {
		try {
			if (!profesionalService.findById(id).isPresent()) {
				return ResponseEntity.notFound().build();
			}

			profesionalService.deleteById(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}