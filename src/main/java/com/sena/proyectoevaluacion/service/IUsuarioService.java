package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

	// CRUD básico
	List<Usuario> findAll();

	Optional<Usuario> findById(Integer id);

	Usuario save(Usuario usuario);

	void deleteById(Integer id);

	// Métodos específicos
	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean autenticarUsuario(String email, String password);

	// Registro de usuarios
	Usuario registrarUsuario(Usuario usuario);

	Usuario registrarUsuario(String nombre, String email, String telefono, String password);

	// Métodos con relaciones
	Optional<Usuario> findByIdWithProfesional(Integer id);

	Optional<Usuario> findByIdWithCitas(Integer id);

	Optional<Usuario> findByIdWithAllRelations(Integer id);

	// Registro de profesional
	Usuario registrarProfesional(Usuario usuario, String especialidad, String horarioDisponible);

	boolean esProfesional(Integer usuarioId);

	// Métodos estadísticos (agregar estos si no existen)
	long countUsuariosRegistradosHoy();

	long countTotalUsuarios();

	List<Usuario> findUsuariosProfesionales();
}