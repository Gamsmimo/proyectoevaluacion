// IProfesionalRepository.java
package com.sena.proyectoevaluacion.repository;

import com.sena.proyectoevaluacion.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IProfesionalRepository extends JpaRepository<Profesional, Integer> {
	Optional<Profesional> findByUsuarioId(Integer usuarioId);

	boolean existsByUsuarioId(Integer usuarioId);
}