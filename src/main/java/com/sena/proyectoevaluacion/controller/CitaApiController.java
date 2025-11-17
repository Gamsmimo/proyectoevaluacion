package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Cita;
import com.sena.proyectoevaluacion.service.ICitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaApiController {

	@Autowired
	private ICitaService citaService;

	// GET - Obtener todas las citas
	@GetMapping
	public ResponseEntity<List<Cita>> obtenerTodasCitas() {
		try {
			List<Cita> citas = citaService.findAll();
			return ResponseEntity.ok(citas);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Obtener cita por ID
	@GetMapping("/{id}")
	public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Integer id) {
		try {
			Optional<Cita> cita = citaService.findById(id);
			return cita.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Obtener citas por usuario
	@GetMapping("/usuario/{usuarioId}")
	public ResponseEntity<List<Cita>> obtenerCitasPorUsuario(@PathVariable Integer usuarioId) {
		try {
			List<Cita> citas = citaService.findByUsuarioId(usuarioId);
			return ResponseEntity.ok(citas);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Obtener citas por profesional
	@GetMapping("/profesional/{profesionalId}")
	public ResponseEntity<List<Cita>> obtenerCitasPorProfesional(@PathVariable Integer profesionalId) {
		try {
			List<Cita> citas = citaService.findByProfesionalId(profesionalId);
			return ResponseEntity.ok(citas);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// POST - Crear nueva cita
	@PostMapping
	public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
		try {
			// Validaciones básicas
			if (cita.getFechaHora() == null || cita.getUsuario() == null || cita.getProfesional() == null
					|| cita.getServicio() == null) {
				return ResponseEntity.badRequest().build();
			}

			Cita citaCreada = citaService.save(cita);
			return ResponseEntity.ok(citaCreada);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// PUT - Actualizar cita
	@PutMapping("/{id}")
	public ResponseEntity<Cita> actualizarCita(@PathVariable Integer id, @RequestBody Cita cita) {
		try {
			Optional<Cita> citaExistente = citaService.findById(id);
			if (citaExistente.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			Cita citaActual = citaExistente.get();
			citaActual.setFechaHora(cita.getFechaHora());
			citaActual.setEstado(cita.getEstado());
			// Actualizar otras propiedades según sea necesario

			Cita citaActualizada = citaService.save(citaActual);
			return ResponseEntity.ok(citaActualizada);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// DELETE - Eliminar cita
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarCita(@PathVariable Integer id) {
		try {
			if (!citaService.findById(id).isPresent()) {
				return ResponseEntity.notFound().build();
			}

			citaService.deleteById(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// PUT - Cambiar estado de cita
	@PutMapping("/{id}/estado")
	public ResponseEntity<Cita> cambiarEstadoCita(@PathVariable Integer id, @RequestParam String estado) {
		try {
			Optional<Cita> citaExistente = citaService.findById(id);
			if (citaExistente.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			Cita cita = citaExistente.get();
			cita.setEstado(estado);

			Cita citaActualizada = citaService.save(cita);
			return ResponseEntity.ok(citaActualizada);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}