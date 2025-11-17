package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.model.Cita;
import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.service.IProfesionalService;
import com.sena.proyectoevaluacion.service.IUsuarioService;
import com.sena.proyectoevaluacion.service.ICitaService;
import com.sena.proyectoevaluacion.service.IServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

	// ✅ MÉTODO AUXILIAR PARA OBTENER EL USUARIO AUTENTICADO
	private Usuario getUsuarioAutenticado() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
		return usuarioOpt.orElse(null);
	}

	// ✅ MÉTODO AUXILIAR PARA VERIFICAR SI ES PROFESIONAL
	private boolean esProfesional(Usuario usuario) {
		return usuario != null && usuario.getProfesional() != null;
	}

	// En ProfesionalController.java - AGREGAR ESTE MÉTODO DE DEBUG
	@GetMapping("/debug-endpoints")
	@ResponseBody
	public String debugEndpoints() {
		return "Endpoints disponibles:\n" + "POST /profesional/cita/{citaId}/estado\n"
				+ "POST /profesional/crear-servicio\n" + "POST /profesional/actualizar-servicio\n"
				+ "POST /profesional/eliminar-servicio/{id}\n" + "GET /profesional/servicios\n"
				+ "GET /profesional/servicio/{id}";
	}

	// Vista del dashboard del profesional - VERSIÓN ACTUALIZADA
	@GetMapping("/dashboard")
	public String dashboard(Model model, RedirectAttributes redirectAttributes) {
		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		// Verificar si el usuario es profesional
		if (!esProfesional(usuario)) {
			redirectAttributes.addFlashAttribute("error", "No tienes permisos de profesional");
			return "redirect:/usuarios/home";
		}

		// Cargar usuario con relaciones actualizadas
		Optional<Usuario> usuarioActualizado = usuarioService.findByIdWithAllRelations(usuario.getId());
		if (usuarioActualizado.isPresent()) {
			usuario = usuarioActualizado.get();
		}

		Profesional profesional = usuario.getProfesional();
		if (profesional == null) {
			redirectAttributes.addFlashAttribute("error", "No se encontró información profesional");
			return "redirect:/usuarios/home";
		}

		// Obtener citas por estado
		List<Cita> citasPendientes = citaService.findByProfesionalIdAndEstado(profesional.getId(), "PENDIENTE");
		List<Cita> citasAceptadas = citaService.findByProfesionalIdAndEstado(profesional.getId(), "ACEPTADA");
		List<Cita> citasCompletadas = citaService.findByProfesionalIdAndEstado(profesional.getId(), "COMPLETADA");
		List<Cita> citasRechazadas = citaService.findByProfesionalIdAndEstado(profesional.getId(), "RECHAZADA");

		model.addAttribute("usuario", usuario);
		model.addAttribute("profesional", profesional);
		model.addAttribute("citasPendientes", citasPendientes);
		model.addAttribute("citasAceptadas", citasAceptadas);
		model.addAttribute("citasCompletadas", citasCompletadas);
		model.addAttribute("citasRechazadas", citasRechazadas);

		return "Profesional/profesional"; // Asegúrate que esta ruta sea correcta
	}

	// Vista para registro de profesional - ACTUALIZADO
	@GetMapping("/registro")
	public String registroProfesional(Model model, RedirectAttributes redirectAttributes) {
		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		if (esProfesional(usuario)) {
			redirectAttributes.addFlashAttribute("info", "Ya estás registrado como profesional");
			return "redirect:/profesional/dashboard";
		}

		model.addAttribute("usuario", usuario);
		return "Profesional/registro";
	}

	// Procesar registro de profesional - ACTUALIZADO
	@PostMapping("/registro")
	public String procesarRegistroProfesional(@RequestParam String especialidad, @RequestParam String horarioDisponible,
			RedirectAttributes redirectAttributes) {

		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		// Verificar si ya es profesional
		if (esProfesional(usuario)) {
			redirectAttributes.addFlashAttribute("error", "Ya estás registrado como profesional");
			return "redirect:/profesional/dashboard";
		}

		try {
			// Validar campos
			if (especialidad == null || especialidad.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "La especialidad es requerida");
				return "redirect:/profesional/registro";
			}

			if (horarioDisponible == null || horarioDisponible.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "El horario disponible es requerido");
				return "redirect:/profesional/registro";
			}

			// Convertir String a LocalDateTime para horarioDisponible
			LocalDateTime horario = parseHorarioDisponible(horarioDisponible);

			// Crear y guardar el profesional
			Profesional profesional = new Profesional(especialidad.trim(), horario, usuario);
			Profesional profesionalGuardado = profesionalService.save(profesional);

			System.out.println("✅ Profesional registrado exitosamente. ID: " + profesionalGuardado.getId());
			System.out.println("👤 Usuario: " + usuario.getEmail());
			System.out.println("🎯 Especialidad: " + especialidad);

			redirectAttributes.addFlashAttribute("success", "¡Registro como profesional exitoso!");
			return "redirect:/profesional/dashboard";

		} catch (Exception e) {
			System.err.println("❌ Error en registro profesional: " + e.getMessage());
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Error en el registro: " + e.getMessage());
			return "redirect:/profesional/registro";
		}
	}

	// Vista para editar información profesional - ACTUALIZADO
	@GetMapping("/editar")
	public String editarInformacion(Model model, RedirectAttributes redirectAttributes) {
		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		if (!esProfesional(usuario)) {
			redirectAttributes.addFlashAttribute("error", "No tienes permisos de profesional");
			return "redirect:/usuarios/home";
		}

		// Cargar usuario con relaciones
		Optional<Usuario> usuarioActualizado = usuarioService.findByIdWithAllRelations(usuario.getId());
		if (usuarioActualizado.isPresent()) {
			usuario = usuarioActualizado.get();
		}

		Profesional profesional = usuario.getProfesional();
		if (profesional == null) {
			redirectAttributes.addFlashAttribute("error", "No se encontró información profesional");
			return "redirect:/usuarios/home";
		}

		model.addAttribute("usuario", usuario);
		model.addAttribute("profesional", profesional);
		return "Profesional/editar-profesional";
	}

	// Procesar actualización de información - ACTUALIZADO
	@PostMapping("/actualizar")
	public String actualizarInformacion(@RequestParam String especialidad, @RequestParam String horarioDisponible,
			@RequestParam String email, @RequestParam String telefono, RedirectAttributes redirectAttributes) {

		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		if (!esProfesional(usuario)) {
			redirectAttributes.addFlashAttribute("error", "No tienes permisos de profesional");
			return "redirect:/usuarios/home";
		}

		try {
			// Validaciones básicas
			if (email == null || email.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "El email es obligatorio");
				return "redirect:/profesional/editar";
			}

			if (telefono == null || telefono.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "El teléfono es obligatorio");
				return "redirect:/profesional/editar";
			}

			if (especialidad == null || especialidad.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "La especialidad es obligatoria");
				return "redirect:/profesional/editar";
			}

			if (horarioDisponible == null || horarioDisponible.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "El horario disponible es obligatorio");
				return "redirect:/profesional/editar";
			}

			// Actualizar información del usuario
			usuario.setEmail(email.trim());
			usuario.setTelefono(telefono.trim());
			usuarioService.save(usuario);

			// Cargar usuario con relaciones actualizadas
			Optional<Usuario> usuarioConRelaciones = usuarioService.findByIdWithAllRelations(usuario.getId());
			if (usuarioConRelaciones.isPresent()) {
				usuario = usuarioConRelaciones.get();
			}

			// Actualizar información del profesional
			Profesional profesional = usuario.getProfesional();
			if (profesional != null) {
				LocalDateTime horario = parseHorarioDisponible(horarioDisponible.trim());

				profesional.setEspecialidad(especialidad.trim());
				profesional.setHorarioDisponible(horario);

				Profesional profesionalActualizado = profesionalService.save(profesional);

				System.out.println("✅ Información profesional actualizada - ID: " + profesionalActualizado.getId());
				System.out.println("📝 Nueva especialidad: " + especialidad);
				System.out.println("⏰ Nuevo horario: " + horarioDisponible);
			} else {
				redirectAttributes.addFlashAttribute("error", "No se encontró la información profesional");
				return "redirect:/profesional/editar";
			}

			redirectAttributes.addFlashAttribute("success", "Información actualizada exitosamente");
			return "redirect:/profesional/dashboard";

		} catch (Exception e) {
			System.err.println("❌ Error al actualizar información profesional: " + e.getMessage());
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Error al actualizar información: " + e.getMessage());
			return "redirect:/profesional/editar";
		}
	}

	// Eliminar cuenta del profesional - ACTUALIZADO
	@PostMapping("/eliminar-cuenta")
	public String eliminarCuenta(RedirectAttributes redirectAttributes) {
		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			Profesional profesional = usuario.getProfesional();
			if (profesional != null) {
				profesionalService.deleteById(profesional.getId());
			}

			usuarioService.deleteById(usuario.getId());

			// Spring Security manejará la invalidación de la sesión

			redirectAttributes.addFlashAttribute("success", "Tu cuenta ha sido eliminada exitosamente");
			return "redirect:/usuarios/login";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error al eliminar la cuenta: " + e.getMessage());
			return "redirect:/profesional/dashboard";
		}
	}

	@GetMapping("/servicios")
	@ResponseBody
	public ResponseEntity<List<Servicio>> obtenerServiciosProfesional() {
		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null || !esProfesional(usuario)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			List<Servicio> servicios = servicioService.findAll();
			return ResponseEntity.ok(servicios);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// ... (mantén los demás métodos como están, pero elimina los parámetros
	// HttpSession)

	// Método auxiliar para parsear horario disponible (mantén este igual)
	private LocalDateTime parseHorarioDisponible(String horarioStr) {
		try {
			if (horarioStr.contains("T")) {
				return LocalDateTime.parse(horarioStr.replace(" ", "T"));
			}

			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2}:\\d{2})");
			java.util.regex.Matcher matcher = pattern.matcher(horarioStr);

			if (matcher.find()) {
				String horaStr = matcher.group(1);
				String[] partesHora = horaStr.split(":");
				int horas = Integer.parseInt(partesHora[0]);
				int minutos = Integer.parseInt(partesHora[1]);

				LocalDateTime ahora = LocalDateTime.now();
				return LocalDateTime.of(ahora.getYear(), ahora.getMonth(), ahora.getDayOfMonth(), horas, minutos);
			}

			System.out.println("⚠️ No se pudo parsear el horario: " + horarioStr + ". Usando horario por defecto.");
			LocalDateTime ahora = LocalDateTime.now();
			return LocalDateTime.of(ahora.getYear(), ahora.getMonth(), ahora.getDayOfMonth(), 9, 0);

		} catch (Exception e) {
			System.err.println("❌ Error al parsear horario disponible: " + horarioStr + " - " + e.getMessage());
			LocalDateTime ahora = LocalDateTime.now();
			return LocalDateTime.of(ahora.getYear(), ahora.getMonth(), ahora.getDayOfMonth(), 9, 0);
		}
	}

	@PostMapping("/eliminar-servicio/{id}")
	@ResponseBody
	public ResponseEntity<?> eliminarServicio(@PathVariable Integer id) {

		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null || !esProfesional(usuario)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
		}

		try {
			servicioService.deleteById(id);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Servicio eliminado correctamente");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al eliminar servicio: " + e.getMessage());
		}
	}

	@PostMapping("/actualizar-servicio")
	@ResponseBody
	public ResponseEntity<?> actualizarServicio(@RequestParam Integer servicioId, @RequestParam String nombreServicio,
			@RequestParam String descripcionServicio, @RequestParam Integer duracionServicio,
			@RequestParam Double precioServicio) {

		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null || !esProfesional(usuario)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
		}

		try {
			Optional<Servicio> servicioOpt = servicioService.findById(servicioId);
			if (servicioOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Servicio no encontrado");
			}

			Servicio servicio = servicioOpt.get();
			servicio.setNombre(nombreServicio);
			servicio.setDescripcion(descripcionServicio);
			servicio.setDuracion(duracionServicio + " minutos");
			servicio.setPrecio(precioServicio);

			Servicio actualizado = servicioService.save(servicio);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("servicio", actualizado);
			response.put("message", "Servicio actualizado exitosamente");

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al actualizar servicio: " + e.getMessage());
		}
	}

	@GetMapping("/servicio/{id}")
	@ResponseBody
	public ResponseEntity<?> obtenerServicio(@PathVariable Integer id) {
		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null || !esProfesional(usuario)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			Optional<Servicio> servicioOpt = servicioService.findById(id);
			if (servicioOpt.isPresent()) {
				return ResponseEntity.ok(servicioOpt.get());
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Servicio no encontrado");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// ✅ AGREGAR ESTE MÉTODO NUEVO EN ProfesionalController.java
	@PostMapping("/cita/{citaId}/estado")
	@ResponseBody
	public ResponseEntity<?> actualizarEstadoCita(@PathVariable Integer citaId, @RequestParam String estado) {
		try {
			System.out.println("📝 Actualizando estado de cita ID: " + citaId + " a: " + estado);

			Usuario usuario = getUsuarioAutenticado();
			if (usuario == null || !esProfesional(usuario)) {
				System.out.println("❌ Usuario no autorizado");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
			}

			Optional<Cita> citaOpt = citaService.findById(citaId);
			if (citaOpt.isEmpty()) {
				System.out.println("❌ Cita no encontrada: " + citaId);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita no encontrada");
			}

			Cita cita = citaOpt.get();

			// Verificar que la cita pertenece a este profesional
			Profesional profesionalUsuario = usuario.getProfesional();
			if (profesionalUsuario == null || !cita.getProfesional().getId().equals(profesionalUsuario.getId())) {
				System.out.println("❌ El profesional no tiene permisos para esta cita");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para esta cita");
			}

			// Validar estado
			List<String> estadosValidos = Arrays.asList("PENDIENTE", "ACEPTADA", "RECHAZADA", "COMPLETADA");
			if (!estadosValidos.contains(estado)) {
				return ResponseEntity.badRequest().body("Estado no válido: " + estado);
			}

			cita.setEstado(estado);
			citaService.save(cita);

			System.out.println("✅ Estado de cita actualizado exitosamente");

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Estado de cita actualizado correctamente");
			response.put("nuevoEstado", estado);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("❌ Error al actualizar estado de cita: " + e.getMessage());
			e.printStackTrace();

			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Error al actualizar estado: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/servicios/crear")
	public String mostrarFormularioServicio(Model model, RedirectAttributes redirectAttributes) {
		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		if (!esProfesional(usuario)) {
			redirectAttributes.addFlashAttribute("error", "No tienes permisos de profesional");
			return "redirect:/usuarios/home";
		}

		model.addAttribute("servicio", new Servicio());
		model.addAttribute("usuario", usuario);
		model.addAttribute("profesional", usuario.getProfesional());

		return "Profesional/crear-servicio"; // Asegúrate de crear esta vista
	}

	@PostMapping("/crear-servicio")
	@ResponseBody
	public ResponseEntity<?> crearServicio(@RequestParam String nombreServicio,
			@RequestParam String descripcionServicio, @RequestParam Integer duracionServicio,
			@RequestParam Double precioServicio) {

		Usuario usuario = getUsuarioAutenticado();
		if (usuario == null || !esProfesional(usuario)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
		}

		try {
			Servicio servicio = new Servicio();
			servicio.setNombre(nombreServicio);
			servicio.setDescripcion(descripcionServicio);
			servicio.setDuracion(duracionServicio + " minutos");
			servicio.setPrecio(precioServicio);

			// IMPORTANTE: NO ASIGNAR profesional porque NO existe en tu BD
			// servicio.setProfesional(...); ❌ ELIMINADO

			Servicio servicioGuardado = servicioService.save(servicio);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("servicio", servicioGuardado);
			response.put("message", "Servicio creado exitosamente");

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Error al crear servicio: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
