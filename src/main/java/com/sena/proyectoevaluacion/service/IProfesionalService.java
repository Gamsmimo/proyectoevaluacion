package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Profesional;
import java.util.List;
import java.util.Optional;

public interface IProfesionalService {
	List<Profesional> findAll();

	Optional<Profesional> findById(Integer id);

	Optional<Profesional> findByUsuarioId(Integer usuarioId);

	Profesional save(Profesional profesional);

	void deleteById(Integer id);

	boolean existsByUsuarioId(Integer usuarioId);
}