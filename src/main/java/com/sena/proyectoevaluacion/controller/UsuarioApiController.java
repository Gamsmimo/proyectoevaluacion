package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioApiController {

	@Autowired
	private IUsuarioService usuarioService;

	// GET - Obtener todos los usuarios
	@GetMapping
	public ResponseEntity<List<Usuario>> obtenerTodosUsuarios() {
		try {
			List<Usuario> usuarios = usuarioService.findAll();
			return ResponseEntity.ok(usuarios);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// GET - Obtener usuario por ID
	@GetMapping("/{id}")
	public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Integer id) {
		try {
			Optional<Usuario> usuario = usuarioService.findById(id);
			return usuario.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// POST - Crear nuevo usuario (recibe JSON)
	@PostMapping
	public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {
		try {
			if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio"));
			}
			if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "El email es obligatorio"));
			}
			if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es obligatoria"));
			}
			if (usuario.getPassword().length() < 6) {
				return ResponseEntity.badRequest()
						.body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
			}
			if (usuarioService.existsByEmail(usuario.getEmail())) {
				return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
			}

			Usuario usuarioCreado = usuarioService.registrarUsuario(usuario);
			usuarioCreado.setPassword(null);
			return ResponseEntity.ok(
					Map.of("success", true, "usuario", usuarioCreado, "message", "Usuario registrado exitosamente"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al registrar usuario: " + e.getMessage()));
		}
	}

	// PUT - Actualizar usuario (recibe JSON)
	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
		try {
			Optional<Usuario> usuarioExistente = usuarioService.findById(id);
			if (usuarioExistente.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			Usuario usuarioActual = usuarioExistente.get();
			if (usuario.getNombre() != null)
				usuarioActual.setNombre(usuario.getNombre());
			if (usuario.getEmail() != null)
				usuarioActual.setEmail(usuario.getEmail());
			if (usuario.getTelefono() != null)
				usuarioActual.setTelefono(usuario.getTelefono());
			if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
				usuarioActual.setPassword(usuario.getPassword());
			}

			Usuario usuarioActualizado = usuarioService.save(usuarioActual);
			usuarioActualizado.setPassword(null); // no devolver contraseña
			return ResponseEntity.ok(Map.of("success", true, "usuario", usuarioActualizado));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al actualizar usuario: " + e.getMessage()));
		}
	}

	// DELETE - Eliminar usuario (no necesita JSON, solo ID en la URL)
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {
		try {
			Optional<Usuario> usuarioExistente = usuarioService.findById(id);
			if (usuarioExistente.isEmpty()) {
				return ResponseEntity.status(404).body(Map.of("success", false, "error", "Usuario no encontrado"));
			}

			usuarioService.deleteById(id);
			return ResponseEntity.ok(Map.of("success", true, "message", "Usuario eliminado"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "error", "Error al eliminar usuario: " + e.getMessage()));
		}
	}

	// POST - Login (recibe JSON)
	@PostMapping("/login")
	public ResponseEntity<?> loginUsuario(@RequestBody Map<String, String> loginData) {
		String email = loginData.get("email");
		String password = loginData.get("password");

		if (usuarioService.autenticarUsuario(email, password)) {
			Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);
			Usuario usuario = usuarioOpt.get();
			usuario.setPassword(null);
			return ResponseEntity.ok(Map.of("success", true, "usuario", usuario));
		} else {
			return ResponseEntity.status(401).body(Map.of("success", false, "error", "Credenciales incorrectas"));
		}
	}
}
