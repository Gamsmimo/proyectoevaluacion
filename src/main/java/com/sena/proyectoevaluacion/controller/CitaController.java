package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.*;
import com.sena.proyectoevaluacion.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

	// Procesar reserva de cita - CORREGIDO
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
				cita.setFechaHora(fechaHora); // Guardar como LocalDateTime
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

	// Mostrar citas del usuario
	@GetMapping("/mis-citas")
	public String misCitas(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		List<Cita> citas = citaService.findByUsuarioId(usuario.getId());
		model.addAttribute("citas", citas != null ? citas : new ArrayList<Cita>());
		return "mis-citas";
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
}