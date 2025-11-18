package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImplement implements IUsuarioService {

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IProfesionalService profesionalService;

	// ✅ AGREGAR PasswordEncoder para Spring Security
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public List<Usuario> findAll() {
		return usuarioRepository.findAll();
	}

	@Override
	public Optional<Usuario> findById(Integer id) {
		return usuarioRepository.findById(id);
	}

	@Override
	@Transactional
	public Usuario save(Usuario usuario) {
		return usuarioRepository.save(usuario);
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {
		try {
			// Opcional: puedes agregar lógica adicional antes de eliminar
			usuarioRepository.deleteById(id);
		} catch (Exception e) {
			throw new RuntimeException("Error al eliminar usuario: " + e.getMessage());
		}
	}

	@Override
	public Optional<Usuario> findByEmail(String email) {
		return usuarioRepository.findByEmail(email);
	}

	@Override
	public boolean existsByEmail(String email) {
		return usuarioRepository.existsByEmail(email);
	}

	@Override
	public boolean autenticarUsuario(String email, String password) {
		try {
			// ✅ USAR PasswordEncoder para verificar la contraseña
			Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

			if (usuarioOpt.isPresent()) {
				Usuario usuario = usuarioOpt.get();
				// Verificar si la contraseña en texto plano coincide con la encriptada
				return passwordEncoder.matches(password, usuario.getPassword());
			}
			return false;

		} catch (Exception e) {
			System.err.println("Error en autenticación: " + e.getMessage());
			return false;
		}
	}

	@Override
	@Transactional
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

			// ✅ ENCRIPTAR CONTRASEÑA ANTES DE GUARDAR
			if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
				String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
				usuario.setPassword(passwordEncriptada);
			}

			// Asegurar que la fecha de registro se establezca como LocalDateTime
			if (usuario.getFechaRegistro() == null) {
				usuario.setFechaRegistro(LocalDateTime.now());
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

	@Override
	@Transactional
	public Usuario registrarUsuario(String nombre, String email, String telefono, String password) {
		// Crear usuario con constructor que acepte estos parámetros
		Usuario usuario = new Usuario();
		usuario.setNombre(nombre);
		usuario.setEmail(email);
		usuario.setTelefono(telefono);
		usuario.setPassword(password); // Será encriptado en registrarUsuario(Usuario usuario)
		usuario.setFechaRegistro(LocalDateTime.now());

		return registrarUsuario(usuario);
	}

	@Override
	public Optional<Usuario> findByIdWithProfesional(Integer id) {
		return usuarioRepository.findByIdWithProfesional(id);
	}

	@Override
	public Optional<Usuario> findByIdWithCitas(Integer id) {
		return usuarioRepository.findByIdWithCitas(id);
	}

	@Override
	public Optional<Usuario> findByIdWithAllRelations(Integer id) {
		try {
			Optional<Usuario> usuarioOpt = usuarioRepository.findByIdWithAllRelations(id);
			if (usuarioOpt.isPresent()) {
				Usuario usuario = usuarioOpt.get();
				// Forzar la carga de las relaciones si es necesario
				if (usuario.getProfesional() != null) {
					System.out.println("DEBUG: Usuario " + usuario.getId() + " es profesional");
				}
				return usuarioOpt;
			}
			return Optional.empty();
		} catch (Exception e) {
			System.err.println("Error al cargar usuario con relaciones: " + e.getMessage());
			return usuarioRepository.findById(id);
		}
	}

	@Override
	@Transactional
	public Usuario registrarProfesional(Usuario usuario, String especialidad, String horarioDisponible) {
		// Primero guardar el usuario si no tiene ID
		if (usuario.getId() == null) {
			// ✅ Asegurar que la contraseña se encripte también para profesionales
			if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
				String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
				usuario.setPassword(passwordEncriptada);
			}
			usuario = save(usuario);
		}

		// Convertir String a LocalDateTime para horarioDisponible
		LocalDateTime horario = parseHorarioDisponible(horarioDisponible);

		// Luego crear y guardar el profesional
		Profesional profesional = new Profesional(especialidad, horario, usuario);
		profesionalService.save(profesional);

		return usuario;
	}

	@Override
	public boolean esProfesional(Integer usuarioId) {
		return profesionalService.existsByUsuarioId(usuarioId);
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

	// Métodos estadísticos (implementación)
	@Override
	public long countUsuariosRegistradosHoy() {
		try {
			return usuarioRepository.countUsuariosRegistradosHoy();
		} catch (Exception e) {
			// Si falla, intentar con el método nativo
			try {
				return usuarioRepository.countUsuariosRegistradosHoyNative();
			} catch (Exception ex) {
				System.err.println("Error al contar usuarios registrados hoy: " + ex.getMessage());
				return 0;
			}
		}
	}

	@Override
	public long countTotalUsuarios() {
		try {
			return usuarioRepository.countTotalUsuarios();
		} catch (Exception e) {
			System.err.println("Error al contar total de usuarios: " + e.getMessage());
			return usuarioRepository.count();
		}
	}

	@Override
	public List<Usuario> findUsuariosProfesionales() {
		try {
			return usuarioRepository.findUsuariosProfesionales();
		} catch (Exception e) {
			System.err.println("Error al buscar usuarios profesionales: " + e.getMessage());
			return List.of();
		}
	}
}