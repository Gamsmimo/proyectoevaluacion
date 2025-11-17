package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.model.Cita;
import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.service.IUsuarioService;
import com.sena.proyectoevaluacion.service.ICitaService;
import com.sena.proyectoevaluacion.service.IProfesionalService;
import com.sena.proyectoevaluacion.service.IServicioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IProfesionalService profesionalService;

	@Autowired
	private IServicioService servicioService;

	@Autowired
	private ICitaService citaService;

	// ==================== VISTAS ====================

	@GetMapping("/login")
	public String login() {
		return "Login/login";
	}

	@GetMapping("/registro")
	public String registro() {
		return "Registro/registro";
	}

	@GetMapping("/index")
	public String index() {
		return "index";
	}

	// MÉTODO ÚNICO PARA RESERVAR CITA (GET) - CORREGIDO
	@GetMapping("/reservarcita")
	public String reservarcita(Model model, HttpSession session) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			// Cargar profesionales
			List<Profesional> profesionales = profesionalService.findAllWithUsuario();
			System.out.println("=== DEBUG RESERVAR CITA ===");
			System.out.println("Número de profesionales: " + (profesionales != null ? profesionales.size() : "NULL"));

			if (profesionales != null) {
				for (Profesional p : profesionales) {
					System.out.println("Profesional ID: " + p.getId() + ", Especialidad: " + p.getEspecialidad()
							+ ", Usuario: " + (p.getUsuario() != null ? p.getUsuario().getNombre() : "NULL"));
				}
			}

			// Cargar servicios
			List<Servicio> servicios = servicioService.findAll();
			System.out.println("Número de servicios: " + (servicios != null ? servicios.size() : "NULL"));

			model.addAttribute("profesionales", profesionales != null ? profesionales : new ArrayList<>());
			model.addAttribute("servicios", servicios != null ? servicios : new ArrayList<>());
			model.addAttribute("usuario", usuario);

			return "ReservaCita/reservarcita";

		} catch (Exception e) {
			System.out.println("ERROR en reservarcita: " + e.getMessage());
			e.printStackTrace();

			// En caso de error, pasar listas vacías
			model.addAttribute("profesionales", new ArrayList<Profesional>());
			model.addAttribute("servicios", new ArrayList<Servicio>());
			return "ReservaCita/reservarcita";
		}
	}

	@GetMapping("/perfilusuario")
	public String perfilusuario(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario != null) {
			Optional<Usuario> usuarioActualizado = usuarioService.findById(usuario.getId());
			if (usuarioActualizado.isPresent()) {
				Usuario usuarioConRelaciones = usuarioActualizado.get();
				model.addAttribute("usuario", usuarioConRelaciones);
				session.setAttribute("usuario", usuarioConRelaciones);

				// Ahora podemos acceder directamente a través de la relación
				if (usuarioConRelaciones.getProfesional() != null) {
					model.addAttribute("profesional", usuarioConRelaciones.getProfesional());
				}
			} else {
				model.addAttribute("usuario", usuario);
			}
			return "PerfilUsuario/perfilusuario";
		} else {
			return "redirect:/usuarios/login";
		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/usuarios/login";
	}

	@GetMapping("/home")
	public String home(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario != null) {
			// Cargar citas del usuario
			List<Cita> citasUsuario = citaService.findByUsuarioId(usuario.getId());
			model.addAttribute("citas", citasUsuario);
			model.addAttribute("usuario", usuario);
			return "Home/home";
		} else {
			return "redirect:/usuarios/login";
		}
	}

	@PostMapping("/login")
	public String procesarLogin(@RequestParam String email, @RequestParam String password,
			RedirectAttributes redirectAttributes, HttpSession session) {
		try {
			boolean autenticado = usuarioService.autenticarUsuario(email, password);
			if (autenticado) {
				Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
				if (usuarioOpt.isPresent()) {
					Usuario usuario = usuarioOpt.get();

					// Cargar el usuario con todas las relaciones para verificar si es profesional
					Optional<Usuario> usuarioCompleto = usuarioService.findByIdWithAllRelations(usuario.getId());
					if (usuarioCompleto.isPresent()) {
						usuario = usuarioCompleto.get();
					}

					session.setAttribute("usuario", usuario);

					// Verificar si es profesional usando la relación cargada
					if (usuario.getProfesional() != null) {
						System.out.println("DEBUG: Usuario es profesional - ID: " + usuario.getProfesional().getId());
						redirectAttributes.addFlashAttribute("success", "¡Inicio de sesión exitoso como profesional!");
						return "redirect:/profesional/dashboard";
					} else {
						System.out.println("DEBUG: Usuario NO es profesional");
						redirectAttributes.addFlashAttribute("success", "¡Inicio de sesión exitoso!");
						return "redirect:/usuarios/home";
					}
				}
			} else {
				redirectAttributes.addFlashAttribute("error", "Email o contraseña incorrectos");
				return "redirect:/usuarios/login";
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error en el servidor: " + e.getMessage());
			return "redirect:/usuarios/login";
		}
		redirectAttributes.addFlashAttribute("error", "Error en el inicio de sesión");
		return "redirect:/usuarios/login";
	}

	@PostMapping("/registro")
	public String procesarRegistro(@RequestParam String nombre, @RequestParam String email,
			@RequestParam String telefono, @RequestParam String password, @RequestParam String confirmPassword,
			@RequestParam String tipoRegistro, @RequestParam(required = false) String especialidad,
			@RequestParam(required = false) String horarioDisponible, RedirectAttributes redirectAttributes,
			HttpSession session) {

		try {
			if (!password.equals(confirmPassword)) {
				redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
				return "redirect:/usuarios/registro";
			}

			// Crear usuario con constructor que acepte estos parámetros
			Usuario usuario = new Usuario();
			usuario.setNombre(nombre);
			usuario.setEmail(email);
			usuario.setTelefono(telefono);
			usuario.setPassword(password);
			usuario.setFechaRegistro(LocalDateTime.now()); // CAMBIO: Usar LocalDateTime directamente

			Usuario usuarioRegistrado = usuarioService.registrarUsuario(usuario);

			if ("profesional".equals(tipoRegistro)) {
				if (especialidad == null || especialidad.trim().isEmpty()) {
					redirectAttributes.addFlashAttribute("error", "La especialidad es obligatoria para profesionales");
					return "redirect:/usuarios/registro";
				}
				if (horarioDisponible == null || horarioDisponible.trim().isEmpty()) {
					redirectAttributes.addFlashAttribute("error",
							"El horario disponible es obligatorio para profesionales");
					return "redirect:/usuarios/registro";
				}

				// Convertir String a LocalDateTime para horarioDisponible
				LocalDateTime horario = parseHorarioDisponible(horarioDisponible);

				// Crear profesional usando la relación
				Profesional profesional = new Profesional(especialidad, horario, usuarioRegistrado);
				profesionalService.save(profesional);

				// Recargar el usuario con las relaciones actualizadas
				Optional<Usuario> usuarioActualizado = usuarioService
						.findByIdWithAllRelations(usuarioRegistrado.getId());
				if (usuarioActualizado.isPresent()) {
					session.setAttribute("usuario", usuarioActualizado.get());
				} else {
					session.setAttribute("usuario", usuarioRegistrado);
				}

				redirectAttributes.addFlashAttribute("success",
						"¡Registro como profesional exitoso! Bienvenido " + nombre);
				return "redirect:/profesional/dashboard";
			}

			redirectAttributes.addFlashAttribute("success",
					"¡Registro exitoso! Bienvenido " + nombre + ". Ahora puedes iniciar sesión.");
			return "redirect:/usuarios/login";

		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/usuarios/registro";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error en el registro: " + e.getMessage());
			return "redirect:/usuarios/registro";
		}
	}

	// MÉTODO PARA PROCESAR LA RESERVA DE CITA (POST) - CORREGIDO
	@PostMapping("/reservarcita")
	public String procesarReservaCita(@RequestParam String servicioId, @RequestParam String profesionalId,
			@RequestParam String fecha, @RequestParam String hora, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			// Validar campos requeridos
			if (servicioId == null || servicioId.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "El servicio es obligatorio");
				return "redirect:/usuarios/reservarcita";
			}

			if (profesionalId == null || profesionalId.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "El profesional es obligatorio");
				return "redirect:/usuarios/reservarcita";
			}

			if (fecha == null || fecha.trim().isEmpty() || hora == null || hora.trim().isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "La fecha y hora son obligatorias");
				return "redirect:/usuarios/reservarcita";
			}

			// Combinar fecha y hora y convertir a LocalDateTime
			String fechaHoraStr = fecha + "T" + hora; // Formato ISO para LocalDateTime
			LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr);

			// Obtener las entidades completas usando las relaciones
			Optional<Servicio> servicioOpt = servicioService.findById(Integer.parseInt(servicioId));
			Optional<Profesional> profesionalOpt = profesionalService.findById(Integer.parseInt(profesionalId));

			if (servicioOpt.isEmpty() || profesionalOpt.isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "Servicio o profesional no encontrado");
				return "redirect:/usuarios/reservarcita";
			}

			// Verificar disponibilidad usando LocalDateTime
			boolean disponible = citaService.verificarDisponibilidad(Integer.parseInt(profesionalId), fechaHora);

			if (!disponible) {
				redirectAttributes.addFlashAttribute("error", "El profesional no está disponible en ese horario");
				return "redirect:/usuarios/reservarcita";
			}

			// Crear la cita usando las relaciones JPA
			Cita cita = new Cita();
			cita.setFechaHora(fechaHora); // CAMBIO: Guardar como LocalDateTime
			cita.setEstado("Pendiente");
			cita.setUsuario(usuario);
			cita.setServicio(servicioOpt.get());
			cita.setProfesional(profesionalOpt.get());

			// Guardar la cita
			Cita citaGuardada = citaService.save(cita);

			System.out.println("Cita guardada exitosamente - ID: " + citaGuardada.getId());

			// Formatear fecha para el mensaje de éxito
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
			String fechaFormateada = fechaHora.format(formatter);

			redirectAttributes.addFlashAttribute("success",
					"¡Cita reservada exitosamente! Te esperamos el " + fechaFormateada);
			return "redirect:/usuarios/home";

		} catch (NumberFormatException e) {
			redirectAttributes.addFlashAttribute("error", "ID de servicio o profesional inválido");
			return "redirect:/usuarios/reservarcita";
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("error", "Error al reservar la cita: " + e.getMessage());
			return "redirect:/usuarios/reservarcita";
		}
	}

	// Método para actualizar perfil de usuario
	@PostMapping("/actualizar-perfil")
	public String actualizarPerfil(@RequestParam String nombre, @RequestParam String email,
			@RequestParam String telefono, HttpSession session, RedirectAttributes redirectAttributes) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			// Actualizar información del usuario
			usuario.setNombre(nombre);
			usuario.setEmail(email);
			usuario.setTelefono(telefono);

			Usuario usuarioActualizado = usuarioService.save(usuario);
			session.setAttribute("usuario", usuarioActualizado);

			redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
			return "redirect:/usuarios/perfilusuario";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
			return "redirect:/usuarios/perfilusuario";
		}
	}

	// En tu controlador existente - agrega estos métodos
	@GetMapping("/registro-profesional")
	public String mostrarRegistroProfesional(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "registro-profesional";
	}

	@PostMapping("/registro-profesional")
	public String registrarProfesional(@ModelAttribute Usuario usuario, @RequestParam String especialidad,
			@RequestParam String horarioDisponible, Model model) {
		try {
			Usuario usuarioGuardado = usuarioService.registrarProfesional(usuario, especialidad, horarioDisponible);
			model.addAttribute("exito", "Profesional registrado correctamente");
			return "redirect:/login-profesional";
		} catch (Exception e) {
			model.addAttribute("error", "Error en el registro: " + e.getMessage());
			return "registro-profesional";
		}
	}

	@GetMapping("/login-profesional")
	public String mostrarLoginProfesional() {
		return "login-profesional";
	}

	@PostMapping("/login-profesional")
	public String loginProfesional(@RequestParam String email, @RequestParam String password, HttpSession session,
			Model model) {
		try {
			// Autenticar usuario
			boolean autenticado = usuarioService.autenticarUsuario(email, password);

			if (autenticado) {
				Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);

				if (usuarioOpt.isPresent()) {
					Usuario usuario = usuarioOpt.get();

					// Verificar si es profesional
					if (usuarioService.esProfesional(usuario.getId())) {
						// Cargar usuario con relaciones
						Optional<Usuario> usuarioCompleto = usuarioService.findByIdWithAllRelations(usuario.getId());
						if (usuarioCompleto.isPresent()) {
							usuario = usuarioCompleto.get();
						}

						// Guardar en sesión
						session.setAttribute("usuario", usuario);
						if (usuario.getProfesional() != null) {
							session.setAttribute("profesional", usuario.getProfesional());
						}
						session.setAttribute("tipoUsuario", "profesional");

						return "redirect:/profesional/dashboard";
					} else {
						model.addAttribute("error", "No tienes permisos de profesional");
						return "login-profesional";
					}
				}
			}

			model.addAttribute("error", "Credenciales inválidas");
			return "login-profesional";

		} catch (Exception e) {
			model.addAttribute("error", "Error en el login: " + e.getMessage());
			return "login-profesional";
		}
	}

	@GetMapping("/profesional/dashboard")
	public String dashboardProfesional(HttpSession session, Model model) {
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

		model.addAttribute("usuario", usuario);
		model.addAttribute("profesional", profesional);

		return "Profesional/profesional"; // Asegúrate de que esta ruta sea correcta
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