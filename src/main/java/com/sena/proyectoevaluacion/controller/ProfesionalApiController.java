package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.service.IProfesionalService;
import com.sena.proyectoevaluacion.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profesionales")
@CrossOrigin(origins = "*")
public class ProfesionalApiController {

	@Autowired
	private IProfesionalService profesionalService;

	@Autowired
	private IUsuarioService usuarioService;

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
	public ResponseEntity<?> crearProfesional(@RequestBody Profesional profesional) {
		try {
			if (profesional.getEspecialidad() == null || profesional.getEspecialidad().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "La especialidad es obligatoria"));
			}

			if (profesional.getHorarioDisponible() == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "El horario disponible es obligatorio"));
			}

			Profesional profesionalCreado = profesionalService.save(profesional);
			return ResponseEntity.ok(Map.of("success", true, "profesional", profesionalCreado, "message",
					"Profesional creado exitosamente"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al crear profesional: " + e.getMessage()));
		}
	}

	@PostMapping("/registrar")
	public ResponseEntity<?> registrarProfesional(@RequestBody Map<String, Object> body) {
		try {
			String especialidad = (String) body.get("especialidad");
			String horarioStr = (String) body.get("horarioDisponible");
			Object usuarioIdObj = body.get("usuarioId");

			if (especialidad == null || especialidad.trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "La especialidad es obligatoria"));
			}
			if (horarioStr == null || horarioStr.trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "El horario disponible es obligatorio"));
			}
			if (usuarioIdObj == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "El usuarioId es obligatorio"));
			}

			Integer usuarioId;
			if (usuarioIdObj instanceof Integer) {
				usuarioId = (Integer) usuarioIdObj;
			} else {
				usuarioId = Integer.parseInt(usuarioIdObj.toString());
			}

			Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
			if (usuarioOpt.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
			}

			LocalDateTime horario = LocalDateTime.parse(horarioStr);
			Usuario usuario = usuarioOpt.get();

			Profesional profesional = new Profesional(especialidad.trim(), horario, usuario);
			Profesional profesionalGuardado = profesionalService.save(profesional);

			return ResponseEntity.ok(Map.of("success", true, "profesional", profesionalGuardado, "message",
					"Profesional registrado y asociado al usuario correctamente"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al registrar profesional: " + e.getMessage()));
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
	public ResponseEntity<?> eliminarProfesional(@PathVariable Integer id) {
		try {
			Optional<Profesional> profesionalOpt = profesionalService.findById(id);
			if (profesionalOpt.isEmpty()) {
				return ResponseEntity.status(404)
						.body(Map.of("success", false, "message", "Profesional no encontrado"));
			}

			profesionalService.deleteById(id);

			return ResponseEntity.ok(Map.of("success", true, "message", "Profesional eliminado correctamente"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "message", "Error al eliminar profesional: " + e.getMessage()));
		}
	}
}