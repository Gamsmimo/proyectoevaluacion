// ProfesionalServiceImplement.java
package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.repository.IProfesionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfesionalServiceImplement implements IProfesionalService {

	@Autowired
	private IProfesionalRepository profesionalRepository;

	@Override
	public List<Profesional> findAll() {
		return profesionalRepository.findAll();
	}

	@Override
	public Optional<Profesional> findById(Integer id) {
		return profesionalRepository.findById(id);
	}

	@Override
	public Optional<Profesional> findByUsuarioId(Integer usuarioId) {
		return profesionalRepository.findByUsuarioId(usuarioId);
	}

	@Override
	public Profesional save(Profesional profesional) {
		return profesionalRepository.save(profesional);
	}

	@Override
	public void deleteById(Integer id) {
		profesionalRepository.deleteById(id);
	}

	@Override
	public boolean existsByUsuarioId(Integer usuarioId) {
		return profesionalRepository.existsByUsuarioId(usuarioId);
	}
}