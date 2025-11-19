package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Cita;
import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.service.ICitaService;
import com.sena.proyectoevaluacion.service.IUsuarioService;
import com.sena.proyectoevaluacion.service.IProfesionalService;
import com.sena.proyectoevaluacion.service.IServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaApiController {

    @Autowired
    private ICitaService citaService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IProfesionalService profesionalService;

    @Autowired
    private IServicioService servicioService;

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

    // POST - Crear nueva cita usando IDs
    @PostMapping
    public ResponseEntity<?> crearCita(@RequestBody Map<String, Object> body) {
        try {
            Object usuarioIdObj = body.get("usuarioId");
            Object profesionalIdObj = body.get("profesionalId");
            Object servicioIdObj = body.get("servicioId");
            String fechaHoraStr = (String) body.get("fechaHora");
            String estado = (String) body.getOrDefault("estado", "PENDIENTE");

            if (usuarioIdObj == null || profesionalIdObj == null || servicioIdObj == null || fechaHoraStr == null
                    || fechaHoraStr.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false,
                                "error", "usuarioId, profesionalId, servicioId y fechaHora son obligatorios"));
            }

            Integer usuarioId = Integer.parseInt(usuarioIdObj.toString());
            Integer profesionalId = Integer.parseInt(profesionalIdObj.toString());
            Integer servicioId = Integer.parseInt(servicioIdObj.toString());

            Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Usuario no encontrado"));
            }

            Optional<Profesional> profesionalOpt = profesionalService.findById(profesionalId);
            if (profesionalOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Profesional no encontrado"));
            }

            Optional<Servicio> servicioOpt = servicioService.findById(servicioId);
            if (servicioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Servicio no encontrado"));
            }

            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr);

            Cita cita = new Cita();
            cita.setFechaHora(fechaHora);
            cita.setEstado(estado != null ? estado : "PENDIENTE");
            cita.setUsuario(usuarioOpt.get());
            cita.setProfesional(profesionalOpt.get());
            cita.setServicio(servicioOpt.get());

            Cita citaCreada = citaService.save(cita);

            return ResponseEntity.ok(
                    Map.of("success", true, "cita", citaCreada, "message", "Cita creada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "error", "Error al crear cita: " + e.getMessage()));
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