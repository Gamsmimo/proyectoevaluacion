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

	// Métodos esenciales
	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

	// Nuevos métodos para autenticación y registro
	boolean autenticarUsuario(String email, String password);

	Usuario registrarUsuario(Usuario usuario);

	Usuario registrarUsuario(String nombre, String email, String telefono, String password);
}