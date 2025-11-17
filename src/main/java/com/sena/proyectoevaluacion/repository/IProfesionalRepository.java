package com.sena.proyectoevaluacion.repository;

import com.sena.proyectoevaluacion.model.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProfesionalRepository extends JpaRepository<Profesional, Integer> {

	// Método básico de JpaRepository (ya está incluido por extender JpaRepository)
	// List<Profesional> findAll(); // NO necesitas agregarlo, ya existe

	// Buscar profesional por ID de usuario
	@Query("SELECT p FROM Profesional p WHERE p.usuario.id = :usuarioId")
	Optional<Profesional> findByUsuarioId(@Param("usuarioId") Integer usuarioId);

	// Verificar si existe profesional por ID de usuario
	@Query("SELECT COUNT(p) > 0 FROM Profesional p WHERE p.usuario.id = :usuarioId")
	boolean existsByUsuarioId(@Param("usuarioId") Integer usuarioId);

	// Buscar profesional con todas las relaciones
	@Query("SELECT p FROM Profesional p LEFT JOIN FETCH p.usuario LEFT JOIN FETCH p.citas WHERE p.id = :id")
	Optional<Profesional> findByIdWithAllRelations(@Param("id") Integer id);

	// Buscar por especialidad
	List<Profesional> findByEspecialidadContainingIgnoreCase(String especialidad);

	// Buscar todos los profesionales con usuario cargado - VERSIÓN CORREGIDA
	@Query("SELECT p FROM Profesional p JOIN FETCH p.usuario u")
	List<Profesional> findAllWithUsuario();

}