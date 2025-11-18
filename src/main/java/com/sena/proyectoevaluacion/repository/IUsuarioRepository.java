package com.sena.proyectoevaluacion.repository;

import com.sena.proyectoevaluacion.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {

	// Métodos básicos
	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

	// Métodos con relaciones
	@Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.profesional WHERE u.id = :id")
	Optional<Usuario> findByIdWithProfesional(@Param("id") Integer id);

	@Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.citas WHERE u.id = :id")
	Optional<Usuario> findByIdWithCitas(@Param("id") Integer id);

	@Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.profesional LEFT JOIN FETCH u.citas WHERE u.id = :id")
	Optional<Usuario> findByIdWithAllRelations(@Param("id") Integer id);

	// Método corregido para contar usuarios registrados hoy - USANDO LocalDateTime
	@Query("SELECT COUNT(u) FROM Usuario u WHERE FUNCTION('DATE', u.fechaRegistro) = CURRENT_DATE")
	long countUsuariosRegistradosHoy();

	// Método alternativo si el anterior no funciona
	@Query(value = "SELECT COUNT(*) FROM usuarios WHERE DATE(fecha_registro) = CURDATE()", nativeQuery = true)
	long countUsuariosRegistradosHoyNative();

	// Buscar usuarios por nombre
	List<Usuario> findByNombreContainingIgnoreCase(String nombre);

	// Contar total de usuarios
	@Query("SELECT COUNT(u) FROM Usuario u")
	long countTotalUsuarios();

	// Buscar usuarios con rol de profesional
	@Query("SELECT u FROM Usuario u WHERE u.profesional IS NOT NULL")
	List<Usuario> findUsuariosProfesionales();

	// Método adicional para buscar por rango de fechas (opcional)
	@Query("SELECT u FROM Usuario u WHERE u.fechaRegistro BETWEEN :startDate AND :endDate")
	List<Usuario> findByFechaRegistroBetween(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);
}