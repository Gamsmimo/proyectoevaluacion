package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.service.IServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class ServicioApiController {

	@Autowired
	private IServicioService servicioService;

	// GET - Obtener todos los servicios
	@GetMapping
	public ResponseEntity<List<Servicio>> obtenerTodosServicios() {
		try {
			List<Servicio> servicios = servicioService.findAll();
			return ResponseEntity.ok(servicios);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Obtener servicio por ID
	@GetMapping("/{id}")
	public ResponseEntity<Servicio> obtenerServicioPorId(@PathVariable Integer id) {
		try {
			Optional<Servicio> servicio = servicioService.findById(id);
			return servicio.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Buscar servicios por nombre (contiene, ignore case)
	@GetMapping("/buscar")
	public ResponseEntity<List<Servicio>> buscarServiciosPorNombre(@RequestParam String nombre) {
		try {
			List<Servicio> servicios = servicioService.findByNombreContainingIgnoreCase(nombre);
			return ResponseEntity.ok(servicios);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// POST - Crear nuevo servicio
	@PostMapping
	public ResponseEntity<?> crearServicio(@RequestBody Servicio servicio) {
		try {
			if (servicio.getNombre() == null || servicio.getNombre().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "El nombre del servicio es obligatorio"));
			}
			if (servicio.getDescripcion() == null || servicio.getDescripcion().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "La descripción es obligatoria"));
			}
			if (servicio.getDuracion() == null || servicio.getDuracion().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "La duración es obligatoria"));
			}
			if (servicio.getPrecio() == null || servicio.getPrecio() < 0) {
				return ResponseEntity.badRequest().body(Map.of("error", "El precio debe ser mayor o igual a 0"));
			}

			Servicio servicioCreado = servicioService.save(servicio);
			return ResponseEntity
					.ok(Map.of("success", true, "servicio", servicioCreado, "message", "Servicio creado exitosamente"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al crear servicio: " + e.getMessage()));
		}
	}

	// PUT - Actualizar servicio
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarServicio(@PathVariable Integer id, @RequestBody Servicio servicio) {
		try {
			Optional<Servicio> servicioExistenteOpt = servicioService.findById(id);
			if (servicioExistenteOpt.isEmpty()) {
				return ResponseEntity.status(404).body(Map.of("success", false, "error", "Servicio no encontrado"));
			}

			Servicio servicioActual = servicioExistenteOpt.get();
			if (servicio.getNombre() != null && !servicio.getNombre().trim().isEmpty()) {
				servicioActual.setNombre(servicio.getNombre());
			}
			if (servicio.getDescripcion() != null && !servicio.getDescripcion().trim().isEmpty()) {
				servicioActual.setDescripcion(servicio.getDescripcion());
			}
			if (servicio.getDuracion() != null && !servicio.getDuracion().trim().isEmpty()) {
				servicioActual.setDuracion(servicio.getDuracion());
			}
			if (servicio.getPrecio() != null && servicio.getPrecio() >= 0) {
				servicioActual.setPrecio(servicio.getPrecio());
			}

			Servicio servicioActualizado = servicioService.save(servicioActual);
			return ResponseEntity.ok(Map.of("success", true, "servicio", servicioActualizado, "message",
					"Servicio actualizado exitosamente"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al actualizar servicio: " + e.getMessage()));
		}
	}

	// DELETE - Eliminar servicio
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminarServicio(@PathVariable Integer id) {
		try {
			// Verificar si existe
			Optional<Servicio> servicioOpt = servicioService.findById(id);
			if (servicioOpt.isEmpty()) {
				return ResponseEntity.status(404).body(Map.of("success", false, "error", "Servicio no encontrado"));
			}

			// Verificar si tiene citas asociadas
			if (servicioService.tieneCitasAsociadas(id)) {
				return ResponseEntity.status(409).body(Map.of("success", false, "error",
						"No se puede eliminar el servicio porque tiene citas asociadas"));
			}

			servicioService.deleteById(id);
			return ResponseEntity.ok(Map.of("success", true, "message", "Servicio eliminado correctamente"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al eliminar servicio: " + e.getMessage()));
		}
	}
}
