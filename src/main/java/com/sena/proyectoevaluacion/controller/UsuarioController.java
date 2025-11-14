package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.service.IUsuarioService;
import com.sena.proyectoevaluacion.service.IProfesionalService; // ← AÑADIR ESTA IMPORTACIÓN

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired // ← AÑADIR ESTA INYECCIÓN
	private IProfesionalService profesionalService;

	// ==================== API REST ====================

	@GetMapping("/api")
	@ResponseBody
	public List<Usuario> listarUsuarios() {
		return usuarioService.findAll();
	}

	@PostMapping("/api")
	@ResponseBody
	public Usuario crearUsuario(@RequestBody Usuario usuario) {
		return usuarioService.save(usuario);
	}

	@GetMapping("/api/{id}")
	@ResponseBody
	public Usuario obtenerUsuario(@PathVariable Integer id) {
		return usuarioService.findById(id).orElse(null);
	}

	@PutMapping("/api/{id}")
	@ResponseBody
	public Usuario actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
		usuario.setId(id);
		return usuarioService.save(usuario);
	}

	@DeleteMapping("/api/{id}")
	@ResponseBody
	public void eliminarUsuario(@PathVariable Integer id) {
		usuarioService.deleteById(id);
	}

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

	@GetMapping("/reservarcita")
	public String reservarcita() {
		return "ReservaCita/reservarcita";
	}

	@GetMapping("/perfilusuario")
	public String perfilusuario(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario != null) {
			// Recargar el usuario desde la base de datos para obtener datos actualizados
			Optional<Usuario> usuarioActualizado = usuarioService.findById(usuario.getId());
			if (usuarioActualizado.isPresent()) {
				model.addAttribute("usuario", usuarioActualizado.get());
				session.setAttribute("usuario", usuarioActualizado.get()); // Actualizar sesión
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

	@DeleteMapping("/eliminar-cuenta/{id}")
	@ResponseBody
	public Map<String, Object> eliminarCuenta(@PathVariable Integer id, HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Usuario> usuarioOpt = usuarioService.findById(id);

			if (usuarioOpt.isPresent()) {
				Usuario usuario = usuarioOpt.get();

				// Verificar que el usuario que intenta eliminar es el mismo que está en sesión
				Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
				if (usuarioSesion == null || !usuarioSesion.getId().equals(id)) {
					response.put("success", false);
					response.put("message", "No tienes permisos para eliminar esta cuenta");
					return response;
				}

				// Eliminar el usuario de la base de datos
				usuarioService.deleteById(id);

				// Invalidar sesión
				session.invalidate();

				response.put("success", true);
				response.put("message", "Cuenta eliminada correctamente");

				System.out.println("Cuenta eliminada - ID: " + id + ", Email: " + usuario.getEmail());

			} else {
				response.put("success", false);
				response.put("message", "Usuario no encontrado");
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Error al eliminar la cuenta: " + e.getMessage());
			System.err.println("Error al eliminar cuenta: " + e.getMessage());
		}

		return response;
	}

	// ==================== PROCESAMIENTO DE FORMULARIOS ====================
	@GetMapping("/home")
	public String home(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario != null) {
			model.addAttribute("usuario", usuario);
			return "Home/home"; // Usa index.html en lugar de home.html
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
				// Obtener el usuario completo para guardar en sesión
				Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
				if (usuarioOpt.isPresent()) {
					Usuario usuario = usuarioOpt.get();
					session.setAttribute("usuario", usuario);

					// ← AÑADIR ESTA PARTE PARA VERIFICAR SI ES PROFESIONAL
					Optional<Profesional> profesionalOpt = profesionalService.findByUsuarioId(usuario.getId());
					if (profesionalOpt.isPresent()) {
						redirectAttributes.addFlashAttribute("success", "¡Inicio de sesión exitoso como profesional!");
						return "redirect:/profesional/dashboard";
					}
					// ← FIN DE LA PARTE AÑADIDA
				}
				redirectAttributes.addFlashAttribute("success", "¡Inicio de sesión exitoso!");
				return "redirect:/usuarios/home";
			} else {
				redirectAttributes.addFlashAttribute("error", "Email o contraseña incorrectos");
				return "redirect:/usuarios/login";
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error en el servidor: " + e.getMessage());
			return "redirect:/usuarios/login";
		}
	}

	@PostMapping("/registro")
	public String procesarRegistro(@RequestParam String nombre, @RequestParam String email,
			@RequestParam String telefono, @RequestParam String password, @RequestParam String confirmPassword,
			@RequestParam String tipoRegistro, @RequestParam(required = false) String especialidad,
			@RequestParam(required = false) String horarioDisponible, RedirectAttributes redirectAttributes,
			HttpSession session) {

		try {
			// Validar que las contraseñas coincidan
			if (!password.equals(confirmPassword)) {
				redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
				return "redirect:/usuarios/registro";
			}

			// Registrar el usuario
			Usuario usuarioRegistrado = usuarioService.registrarUsuario(nombre, email, telefono, password);

			// Si es profesional, crear registro en tabla profesional
			if ("profesional".equals(tipoRegistro) && especialidad != null && horarioDisponible != null) {
				Profesional profesional = new Profesional(especialidad, horarioDisponible, usuarioRegistrado.getId());
				profesionalService.save(profesional);

				// Guardar en sesión y redirigir al dashboard profesional
				session.setAttribute("usuario", usuarioRegistrado);
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

	@PostMapping("/actualizar")
	@ResponseBody
	public Map<String, Object> actualizarUsuario(@RequestBody Map<String, Object> datos, HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		try {
			Integer id = Integer.parseInt(datos.get("id").toString());
			String nombre = datos.get("nombre").toString();
			String email = datos.get("email").toString();
			String telefono = datos.get("telefono").toString();

			// Verificar que el usuario que edita es el mismo de la sesión
			Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
			if (usuarioSesion == null || !usuarioSesion.getId().equals(id)) {
				response.put("success", false);
				response.put("message", "No tienes permisos para editar este perfil");
				return response;
			}

			// Buscar el usuario en la base de datos
			Optional<Usuario> usuarioOpt = usuarioService.findById(id);
			if (usuarioOpt.isPresent()) {
				Usuario usuario = usuarioOpt.get();

				// Actualizar solo datos básicos (sin foto de perfil)
				usuario.setNombre(nombre);
				usuario.setEmail(email);
				usuario.setTelefono(telefono);

				// Guardar cambios
				Usuario usuarioActualizado = usuarioService.save(usuario);

				// Actualizar sesión
				session.setAttribute("usuario", usuarioActualizado);

				response.put("success", true);
				response.put("message", "Perfil actualizado correctamente");

			} else {
				response.put("success", false);
				response.put("message", "Usuario no encontrado");
			}

		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Error al actualizar: " + e.getMessage());
		}

		return response;
	}
}