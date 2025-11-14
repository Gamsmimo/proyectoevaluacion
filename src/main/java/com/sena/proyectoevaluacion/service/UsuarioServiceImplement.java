package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImplement implements IUsuarioService {

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Override
	public List<Usuario> findAll() {
		return usuarioRepository.findAll();
	}

	@Override
	public Optional<Usuario> findById(Integer id) {
		return usuarioRepository.findById(id);
	}

	@Override
	public Usuario save(Usuario usuario) {
		return usuarioRepository.save(usuario);
	}

	@Override
	public void deleteById(Integer id) {
		usuarioRepository.deleteById(id);
	}

	@Override
	public Optional<Usuario> findByEmail(String email) {
		return usuarioRepository.findByEmail(email);
	}

	@Override
	public boolean existsByEmail(String email) {
		return usuarioRepository.existsByEmail(email);
	}

	/**
	 * Método para autenticar usuario por email y contraseña
	 * 
	 * @param email    Email del usuario
	 * @param password Contraseña del usuario
	 * @return true si las credenciales son correctas, false en caso contrario
	 */
	public boolean autenticarUsuario(String email, String password) {
		try {
			Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndPassword(email, password);
			return usuarioOpt.isPresent();
		} catch (Exception e) {
			System.err.println("Error en autenticación: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Método para registrar un nuevo usuario con validaciones
	 * 
	 * @param usuario Objeto Usuario a registrar
	 * @return Usuario registrado
	 * @throws RuntimeException si el email ya existe o hay errores de validación
	 */
	public Usuario registrarUsuario(Usuario usuario) {
		try {
			// Validar que el usuario no sea nulo
			if (usuario == null) {
				throw new RuntimeException("El usuario no puede ser nulo");
			}

			// Validar email
			if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
				throw new RuntimeException("El email es obligatorio");
			}

			// Validar contraseña
			if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
				throw new RuntimeException("La contraseña es obligatoria");
			}

			// Validar que la contraseña tenga al menos 6 caracteres
			if (usuario.getPassword().length() < 6) {
				throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
			}

			// Verificar si el email ya existe
			if (existsByEmail(usuario.getEmail())) {
				throw new RuntimeException("El email " + usuario.getEmail() + " ya está registrado");
			}

			// Asegurar que la fecha de registro se establezca
			if (usuario.getFechaRegistro() == null) {
				usuario.setFechaRegistro(java.time.LocalDateTime.now());
			}

			System.out.println("Registrando usuario: " + usuario.getEmail());

			// Guardar el usuario en la base de datos
			Usuario usuarioGuardado = usuarioRepository.save(usuario);
			System.out.println("Usuario registrado exitosamente con ID: " + usuarioGuardado.getId());

			return usuarioGuardado;

		} catch (RuntimeException e) {
			// Relanzar excepciones de negocio
			throw e;
		} catch (Exception e) {
			// Capturar cualquier otra excepción
			System.err.println("Error al registrar usuario: " + e.getMessage());
			throw new RuntimeException("Error interno del servidor al registrar usuario: " + e.getMessage());
		}
	}

	/**
	 * Método alternativo para registro con parámetros individuales
	 */
	public Usuario registrarUsuario(String nombre, String email, String telefono, String password) {
		Usuario usuario = new Usuario(nombre, email, telefono, password);
		return registrarUsuario(usuario);
	}
}