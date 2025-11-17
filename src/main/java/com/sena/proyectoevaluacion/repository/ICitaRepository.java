package com.sena.proyectoevaluacion.repository;

import com.sena.proyectoevaluacion.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ICitaRepository extends JpaRepository<Cita, Integer> {

	// Métodos existentes...
	List<Cita> findByUsuarioId(Integer usuarioId);

	List<Cita> findByProfesionalId(Integer profesionalId);

	List<Cita> findByServicioId(Integer servicioId);

	List<Cita> findByEstado(String estado);

	List<Cita> findByProfesionalIdAndEstado(Integer profesionalId, String estado);

	// Método para verificar existencia con String (mantener por compatibilidad)
	@Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.profesional.id = :profesionalId AND c.fechaHora = :fechaHora")
	boolean existsByProfesionalIdAndFechaHora(@Param("profesionalId") Integer profesionalId,
			@Param("fechaHora") String fechaHora);

	// NUEVO MÉTODO: Buscar citas por profesional y LocalDateTime
	@Query("SELECT c FROM Cita c WHERE c.profesional.id = :profesionalId AND c.fechaHora = :fechaHora")
	List<Cita> findByProfesionalIdAndFechaHora(@Param("profesionalId") Integer profesionalId,
			@Param("fechaHora") LocalDateTime fechaHora);

	@Query("SELECT c FROM Cita c LEFT JOIN FETCH c.usuario LEFT JOIN FETCH c.servicio LEFT JOIN FETCH c.profesional WHERE c.id = :id")
	Optional<Cita> findByIdWithAllRelations(@Param("id") Integer id);
}