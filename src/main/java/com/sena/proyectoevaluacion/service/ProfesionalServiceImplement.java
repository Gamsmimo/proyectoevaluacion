package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.repository.IProfesionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
	@Transactional
	public Profesional save(Profesional profesional) {
		try {
			System.out.println("💾 Guardando profesional - ID: " + profesional.getId());
			System.out.println("📝 Especialidad: " + profesional.getEspecialidad());
			System.out.println("⏰ Horario: " + profesional.getHorarioDisponible());

			Profesional guardado = profesionalRepository.save(profesional);
			System.out.println("✅ Profesional guardado exitosamente - ID: " + guardado.getId());

			return guardado;
		} catch (Exception e) {
			System.err.println("❌ Error al guardar profesional: " + e.getMessage());
			throw e;
		}
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {
		profesionalRepository.deleteById(id);
	}

	@Override
	public Optional<Profesional> findByUsuarioId(Integer usuarioId) {
		return profesionalRepository.findByUsuarioId(usuarioId);
	}

	@Override
	public boolean existsByUsuarioId(Integer usuarioId) {
		return profesionalRepository.existsByUsuarioId(usuarioId);
	}

	@Override
	@Transactional
	public void deleteByUsuarioId(Integer usuarioId) {
		Optional<Profesional> profesionalOpt = profesionalRepository.findByUsuarioId(usuarioId);
		profesionalOpt.ifPresent(profesional -> profesionalRepository.deleteById(profesional.getId()));
	}

	@Override
	public List<Profesional> findAllWithUsuario() {
		try {
			List<Profesional> profesionales = profesionalRepository.findAllWithUsuario();
			System.out.println("=== DEBUG PROFESIONAL SERVICE ===");
			System.out.println("Profesionales encontrados: " + (profesionales != null ? profesionales.size() : "NULL"));

			if (profesionales != null && !profesionales.isEmpty()) {
				for (Profesional p : profesionales) {
					System.out.println("Profesional ID: " + p.getId() + ", Usuario: "
							+ (p.getUsuario() != null ? p.getUsuario().getNombre() : "NULL"));
				}
			}
			return profesionales;
		} catch (Exception e) {
			System.out.println("Error en findAllWithUsuario: " + e.getMessage());
			// Fallback al método básico
			return profesionalRepository.findAll();
		}
	}

	@Override
	public Optional<Profesional> findByIdWithAllRelations(Integer id) {
		return profesionalRepository.findByIdWithAllRelations(id);
	}

	@Override
	public List<Profesional> findByEspecialidadContainingIgnoreCase(String especialidad) {
		return profesionalRepository.findByEspecialidadContainingIgnoreCase(especialidad);
	}
	
	@Override
	public List<Profesional> findByEspecialidad(String especialidad) {
	    try {
	        return profesionalRepository.findByEspecialidadContainingIgnoreCase(especialidad);
	    } catch (Exception e) {
	        System.err.println("Error al buscar profesionales por especialidad: " + e.getMessage());
			return new ArrayList<>();
	    }
	}
}