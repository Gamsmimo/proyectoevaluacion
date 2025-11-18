package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.*;
import com.sena.proyectoevaluacion.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/citas")
public class CitaController {

	@Autowired
	private ICitaService citaService;

	@Autowired
	private IProfesionalService profesionalService;

	@Autowired
	private IServicioService servicioService;

	// Procesar reserva de cita
	@PostMapping("/reservar")
	public String reservarCita(@RequestParam("fechaHora") String fechaHoraStr, @RequestParam Integer profesionalId,
			@RequestParam Integer servicioId, @RequestParam(required = false) String descripcion, HttpSession session,
			Model model) {
		try {
			Usuario usuario = (Usuario) session.getAttribute("usuario");
			if (usuario == null) {
				return "redirect:/usuarios/login";
			}

			// Buscar profesional y servicio
			Optional<Profesional> profesionalOpt = profesionalService.findById(profesionalId);
			Optional<Servicio> servicioOpt = servicioService.findById(servicioId);

			if (profesionalOpt.isPresent() && servicioOpt.isPresent()) {
				// Convertir String a LocalDateTime
				LocalDateTime fechaHora = parseFechaHora(fechaHoraStr);

				// Verificar disponibilidad usando LocalDateTime
				boolean disponible = citaService.verificarDisponibilidad(profesionalId, fechaHora);

				if (!disponible) {
					model.addAttribute("error", "El profesional no está disponible en ese horario");
					return "redirect:/citas/reservar";
				}

				// Crear nueva cita
				Cita cita = new Cita();
				cita.setFechaHora(fechaHora);
				cita.setUsuario(usuario);
				cita.setProfesional(profesionalOpt.get());
				cita.setServicio(servicioOpt.get());
				cita.setEstado("Pendiente");

				// Guardar la cita
				citaService.save(cita);

				return "redirect:/citas/mis-citas";
			} else {
				model.addAttribute("error", "Profesional o servicio no encontrado");
				return "redirect:/citas/reservar";
			}

		} catch (Exception e) {
			model.addAttribute("error", "Error al reservar cita: " + e.getMessage());
			return "redirect:/citas/reservar";
		}
	}

	@GetMapping("/mis-citas")
	public String misCitas(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			// Usar el método específico que carga las relaciones
			List<Cita> citasUsuario = citaService.findByUsuarioId(usuario.getId());

			System.out.println("=== DEBUG CONTROLLER - CITAS DEL USUARIO ===");
			System.out.println("Usuario ID: " + usuario.getId());
			System.out.println("Total citas: " + citasUsuario.size());

			// Debug detallado de cada cita
			for (Cita cita : citasUsuario) {
				System.out.println("--- CITA ID: " + cita.getId() + " ---");
				System.out.println("Estado: " + cita.getEstado());
				System.out.println("Fecha: " + cita.getFechaHora());

				if (cita.getServicio() != null) {
					System.out.println("Servicio ID: " + cita.getServicio().getId());
					System.out.println("Servicio Nombre: " + cita.getServicio().getNombre());
					System.out.println("Servicio Descripción: " + cita.getServicio().getDescripcion());
					System.out.println("Servicio Duración: " + cita.getServicio().getDuracion());
					System.out.println("Servicio Precio: " + cita.getServicio().getPrecio());
				} else {
					System.out.println("Servicio: NULL");
				}

				if (cita.getProfesional() != null) {
					System.out.println("Profesional: " + cita.getProfesional().getNombre());
				} else {
					System.out.println("Profesional: NULL");
				}
			}

			model.addAttribute("citas", citasUsuario);
			return "mis-citas";

		} catch (Exception e) {
			System.err.println("ERROR en misCitas: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("citas", new ArrayList<Cita>());
			model.addAttribute("error", "Error al cargar las citas: " + e.getMessage());
			return "mis-citas";
		}
	}

	// Método auxiliar para parsear fecha y hora
	private LocalDateTime parseFechaHora(String fechaHoraStr) {
		try {
			// Si viene en formato ISO (desde input datetime-local)
			if (fechaHoraStr.contains("T")) {
				return LocalDateTime.parse(fechaHoraStr.replace(" ", "T"));
			}
			// Si viene en formato "yyyy-MM-dd HH:mm:ss"
			else if (fechaHoraStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				return LocalDateTime.parse(fechaHoraStr, formatter);
			}
			// Si viene en formato "yyyy-MM-dd HH:mm"
			else if (fechaHoraStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				return LocalDateTime.parse(fechaHoraStr, formatter);
			}
			// Si no se puede parsear, usar la fecha y hora actual
			return LocalDateTime.now();
		} catch (Exception e) {
			System.err.println("Error al parsear fecha y hora: " + fechaHoraStr + " - " + e.getMessage());
			return LocalDateTime.now();
		}
	}

	@GetMapping("/citas-usuario")
	public String citasUsuario(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			// Usar el método específico que carga las relaciones
			List<Cita> citasUsuario = citaService.findByUsuarioId(usuario.getId());

			System.out.println("=== CITAS USUARIO VISTA ===");
			System.out.println("Usuario ID: " + usuario.getId());
			System.out.println("Total citas: " + citasUsuario.size());

			// Debug detallado de cada cita
			for (Cita cita : citasUsuario) {
				System.out.println("--- CITA ID: " + cita.getId() + " ---");
				System.out.println("Estado: " + cita.getEstado());
				System.out.println("Fecha: " + cita.getFechaHora());

				if (cita.getServicio() != null) {
					System.out.println("Servicio: " + cita.getServicio().getNombre());
					System.out.println("Precio: " + cita.getServicio().getPrecio());
				} else {
					System.out.println("Servicio: NULL");
				}

				if (cita.getProfesional() != null) {
					System.out.println("Profesional: " + cita.getProfesional().getNombre());
				} else {
					System.out.println("Profesional: NULL");
				}
			}

			model.addAttribute("citas", citasUsuario);
			model.addAttribute("usuario", usuario);
			return "ReservaCita/citas_usuario";

		} catch (Exception e) {
			System.err.println("ERROR en citasUsuario: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("citas", new ArrayList<Cita>());
			model.addAttribute("error", "Error al cargar las citas: " + e.getMessage());
			return "ReservaCita/citas_usuario";
		}
	}

	@PostMapping("/cancelar-cita/{id}")
	@ResponseBody
	public ResponseEntity<?> cancelarCita(@PathVariable Integer id, HttpSession session) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");

		if (usuario == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
		}

		Optional<Cita> citaOpt = citaService.findById(id);

		if (!citaOpt.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita no encontrada");
		}

		Cita cita = citaOpt.get();

		// Solo el dueño de la cita puede cancelarla
		if (!cita.getUsuario().getId().equals(usuario.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usted no puede cancelar esta cita");
		}

		try {
			citaService.deleteById(id); // 🔥 Elimina completamente la cita
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Cita cancelada correctamente");

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al cancelar la cita: " + e.getMessage());
		}
	}

}