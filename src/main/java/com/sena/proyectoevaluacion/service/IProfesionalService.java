package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Profesional;
import java.util.List;
import java.util.Optional;

public interface IProfesionalService {

	// CRUD básico
	List<Profesional> findAll();

	Optional<Profesional> findById(Integer id);

	Profesional save(Profesional profesional);

	void deleteById(Integer id);

	// Métodos específicos
	Optional<Profesional> findByUsuarioId(Integer usuarioId);

	boolean existsByUsuarioId(Integer usuarioId);

	void deleteByUsuarioId(Integer usuarioId);

	// Métodos con relaciones
	List<Profesional> findAllWithUsuario();

	Optional<Profesional> findByIdWithAllRelations(Integer id);

	List<Profesional> findByEspecialidadContainingIgnoreCase(String especialidad);

	List<Profesional> findByEspecialidad(String especialidad);

}