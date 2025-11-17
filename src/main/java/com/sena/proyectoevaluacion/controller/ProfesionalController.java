package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.model.Cita;
import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.service.IProfesionalService;
import com.sena.proyectoevaluacion.service.IUsuarioService;
import com.sena.proyectoevaluacion.service.ICitaService;
import com.sena.proyectoevaluacion.service.IServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/profesional")
public class ProfesionalController {

	@Autowired
	private IProfesionalService profesionalService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private ICitaService citaService;

	@Autowired
	private IServicioService servicioService;

	// Vista del dashboard del profesional
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		// Cargar usuario con relaciones actualizadas
		Optional<Usuario> usuarioActualizado = usuarioService.findByIdWithAllRelations(usuario.getId());
		if (usuarioActualizado.isPresent()) {
			usuario = usuarioActualizado.get();
			session.setAttribute("usuario", usuario);
		}

		Profesional profesional = usuario.getProfesional();
		if (profesional == null) {
			return "redirect:/usuarios/home";
		}

		List<Cita> citasProfesional = citaService.findByProfesionalId(profesional.getId());

		model.addAttribute("usuario", usuario);
		model.addAttribute("profesional", profesional);
		model.addAttribute("citas", citasProfesional);
		return "Profesional/profesional";
	}

	// Vista para registro de profesional
	@GetMapping("/registro")
	public String registroProfesional(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		if (usuario.getProfesional() != null) {
			return "redirect:/profesional/dashboard";
		}

		model.addAttribute("usuario", usuario);
		return "Profesional/registro";
	}

	// Procesar registro de profesional
	@PostMapping("/registro")
	public String procesarRegistroProfesional(@RequestParam String especialidad, @RequestParam String horarioDisponible,
			HttpSession session, RedirectAttributes redirectAttributes) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			// Convertir String a LocalDateTime para horarioDisponible
			LocalDateTime horario = parseHorarioDisponible(horarioDisponible);

			Profesional profesional = new Profesional(especialidad, horario, usuario);
			profesionalService.save(profesional);

			// Recargar usuario con relaciones
			Optional<Usuario> usuarioActualizado = usuarioService.findByIdWithAllRelations(usuario.getId());
			if (usuarioActualizado.isPresent()) {
				session.setAttribute("usuario", usuarioActualizado.get());
			}

			redirectAttributes.addFlashAttribute("success", "¡Registro como profesional exitoso!");
			return "redirect:/profesional/dashboard";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error en el registro: " + e.getMessage());
			return "redirect:/profesional/registro";
		}
	}

	// Vista para editar información profesional
	@GetMapping("/editar")
	public String editarInformacion(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		// Cargar usuario con relaciones
		Optional<Usuario> usuarioActualizado = usuarioService.findByIdWithAllRelations(usuario.getId());
		if (usuarioActualizado.isPresent()) {
			usuario = usuarioActualizado.get();
			session.setAttribute("usuario", usuario);
		}

		Profesional profesional = usuario.getProfesional();
		if (profesional == null) {
			return "redirect:/usuarios/home";
		}

		model.addAttribute("usuario", usuario);
		model.addAttribute("profesional", profesional);
		return "Profesional/editar-profesional";
	}

	// Procesar actualización de información
	@PostMapping("/actualizar")
	public String actualizarInformacion(@RequestParam String especialidad, @RequestParam String horarioDisponible,
			@RequestParam String email, @RequestParam String telefono, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			usuario.setEmail(email);
			usuario.setTelefono(telefono);
			usuarioService.save(usuario);

			// Cargar usuario con relaciones actualizadas
			Optional<Usuario> usuarioActualizado = usuarioService.findByIdWithAllRelations(usuario.getId());
			if (usuarioActualizado.isPresent()) {
				usuario = usuarioActualizado.get();
			}

			Profesional profesional = usuario.getProfesional();
			if (profesional != null) {
				// Convertir String a LocalDateTime
				LocalDateTime horario = parseHorarioDisponible(horarioDisponible);

				profesional.setEspecialidad(especialidad);
				profesional.setHorarioDisponible(horario);
				profesionalService.save(profesional);
			}

			session.setAttribute("usuario", usuario);

			redirectAttributes.addFlashAttribute("success", "Información actualizada exitosamente");
			return "redirect:/profesional/dashboard";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error al actualizar información: " + e.getMessage());
			return "redirect:/profesional/editar";
		}
	}

	// Eliminar cuenta del profesional
	@PostMapping("/eliminar-cuenta")
	public String eliminarCuenta(HttpSession session, RedirectAttributes redirectAttributes) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			Profesional profesional = usuario.getProfesional();
			if (profesional != null) {
				profesionalService.deleteById(profesional.getId());
			}

			usuarioService.deleteById(usuario.getId());

			session.invalidate();

			redirectAttributes.addFlashAttribute("success", "Tu cuenta ha sido eliminada exitosamente");
			return "redirect:/usuarios/login";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error al eliminar la cuenta: " + e.getMessage());
			return "redirect:/profesional/dashboard";
		}
	}

	// Método para que el profesional actualice el estado de una cita
	@PostMapping("/cita/{citaId}/estado")
	public String actualizarEstadoCita(@PathVariable Integer citaId, @RequestParam String estado, HttpSession session,
			RedirectAttributes redirectAttributes) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null || usuario.getProfesional() == null) {
			return "redirect:/usuarios/login";
		}

		try {
			Optional<Cita> citaOpt = citaService.findById(citaId);
			if (citaOpt.isPresent()) {
				Cita cita = citaOpt.get();
				if (cita.getProfesional().getId().equals(usuario.getProfesional().getId())) {
					cita.setEstado(estado);
					citaService.save(cita);
					redirectAttributes.addFlashAttribute("success", "Estado de la cita actualizado exitosamente");
				} else {
					redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar esta cita");
				}
			} else {
				redirectAttributes.addFlashAttribute("error", "Cita no encontrada");
			}
			return "redirect:/profesional/dashboard";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error al actualizar el estado: " + e.getMessage());
			return "redirect:/profesional/dashboard";
		}
	}

	// Método para crear servicio (AJAX)
	@PostMapping("/crear-servicio")
	@ResponseBody
	public ResponseEntity<?> crearServicio(@RequestParam String nombreServicio,
			@RequestParam String descripcionServicio, @RequestParam Integer duracionServicio,
			@RequestParam Double precioServicio, HttpSession session) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null || usuario.getProfesional() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
		}

		try {
			Profesional profesional = usuario.getProfesional();

			Servicio servicio = new Servicio();
			servicio.setNombre(nombreServicio);
			servicio.setDescripcion(descripcionServicio);
			servicio.setDuracion(duracionServicio + " minutos");
			servicio.setPrecio(precioServicio);

			Servicio servicioGuardado = servicioService.save(servicio);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Servicio creado exitosamente");
			response.put("servicio", servicioGuardado);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Error al crear servicio: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// API REST para profesionales
	@GetMapping("/api")
	@ResponseBody
	public List<Profesional> listarProfesionales() {
		return profesionalService.findAll();
	}

	@GetMapping("/api/{id}")
	@ResponseBody
	public Profesional obtenerProfesional(@PathVariable Integer id) {
		return profesionalService.findById(id).orElse(null);
	}

	// Método auxiliar para parsear horario disponible
	private LocalDateTime parseHorarioDisponible(String horarioStr) {
		try {
			// Si el horario viene en formato ISO (desde input datetime-local)
			if (horarioStr.contains("T")) {
				return LocalDateTime.parse(horarioStr.replace(" ", "T"));
			}
			// Si viene en formato legible, intentar parsear
			else if (horarioStr.matches(".*\\d{1,2}:\\d{2}.*")) {
				// Para formatos como "Lunes a Viernes 08:00-18:00"
				// Usar la fecha actual pero con el horario especificado
				LocalDateTime now = LocalDateTime.now();
				// Extraer la primera hora encontrada (hora de inicio)
				String[] parts = horarioStr.split("\\D+");
				for (String part : parts) {
					if (part.length() >= 3) { // Buscar patrones de hora como "800", "0830", etc.
						String horaStr = part.length() == 3 ? "0" + part.charAt(0) + ":" + part.substring(1)
								: part.substring(0, 2) + ":" + part.substring(2);
						return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
								Integer.parseInt(horaStr.split(":")[0]), Integer.parseInt(horaStr.split(":")[1]));
					}
				}
			}
			// Si no se puede parsear, usar la fecha y hora actual
			return LocalDateTime.now();
		} catch (Exception e) {
			System.err.println("Error al parsear horario disponible: " + horarioStr + " - " + e.getMessage());
			return LocalDateTime.now();
		}
	}
}