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

	// GET - Obtener usuario por email
	@GetMapping("/email/{email}")
	public ResponseEntity<Usuario> obtenerUsuarioPorEmail(@PathVariable String email) {
		try {
			Optional<Usuario> usuario = usuarioService.findByEmail(email);
			return usuario.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// POST - Crear nuevo usuario
	@PostMapping
	public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
		try {
			// Validaciones básicas
			if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
				return ResponseEntity.badRequest().build();
			}

			// Verificar si el email ya existe
			if (usuarioService.existsByEmail(usuario.getEmail())) {
				return ResponseEntity.badRequest().build();
			}

			Usuario usuarioCreado = usuarioService.registrarUsuario(usuario);
			return ResponseEntity.ok(usuarioCreado);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// PUT - Actualizar usuario
	@PutMapping("/{id}")
	public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
		try {
			Optional<Usuario> usuarioExistente = usuarioService.findById(id);
			if (usuarioExistente.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			Usuario usuarioActual = usuarioExistente.get();
			usuarioActual.setNombre(usuario.getNombre());
			usuarioActual.setEmail(usuario.getEmail());
			usuarioActual.setTelefono(usuario.getTelefono());

			// Solo actualizar password si se proporciona uno nuevo
			if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
				usuarioActual.setPassword(usuario.getPassword());
			}

			Usuario usuarioActualizado = usuarioService.save(usuarioActual);
			return ResponseEntity.ok(usuarioActualizado);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// DELETE - Eliminar usuario
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
		try {
			if (!usuarioService.findById(id).isPresent()) {
				return ResponseEntity.notFound().build();
			}

			usuarioService.deleteById(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/estadisticas")
	public ResponseEntity<?> obtenerEstadisticas() {
		try {
			long totalUsuarios = usuarioService.countTotalUsuarios();
			long usuariosHoy = usuarioService.countUsuariosRegistradosHoy();
			List<Usuario> profesionales = usuarioService.findUsuariosProfesionales();

			// ✅ SOLUCIÓN CON MAP - Más limpio
			Map<String, Object> estadisticas = new HashMap<>();
			estadisticas.put("totalUsuarios", totalUsuarios);
			estadisticas.put("usuariosRegistradosHoy", usuariosHoy);
			estadisticas.put("totalProfesionales", profesionales.size());

			return ResponseEntity.ok(estadisticas);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
