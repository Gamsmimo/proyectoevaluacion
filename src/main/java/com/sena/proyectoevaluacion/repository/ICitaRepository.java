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

	// Método optimizado para cargar todas las relaciones
	@Query("SELECT c FROM Cita c WHERE c.profesional.id = :profesionalId AND c.estado = :estado ORDER BY c.fechaHora ASC")
	List<Cita> findByProfesionalIdAndEstado(@Param("profesionalId") Integer profesionalId,
			@Param("estado") String estado);

	// BUSCAR CITAS POR USUARIO (FALTANTE - AGREGADO)
	@Query("SELECT c FROM Cita c WHERE c.usuario.id = :usuarioId ORDER BY c.fechaHora DESC")
	List<Cita> findByUsuarioId(@Param("usuarioId") Integer usuarioId);

	List<Cita> findByProfesionalId(Integer profesionalId);

	List<Cita> findByServicioId(Integer servicioId);

	List<Cita> findByEstado(String estado);

	// MÉTODO MEJORADO: Buscar citas por profesional y LocalDateTime
	@Query("SELECT c FROM Cita c WHERE c.profesional.id = :profesionalId AND c.fechaHora = :fechaHora")
	List<Cita> findByProfesionalIdAndFechaHora(@Param("profesionalId") Integer profesionalId,
			@Param("fechaHora") LocalDateTime fechaHora);

	// MÉTODO ALTERNATIVO: Para verificar disponibilidad (más eficiente)
	@Query("SELECT COUNT(c) FROM Cita c WHERE c.profesional.id = :profesionalId AND c.fechaHora = :fechaHora AND c.estado IN ('PENDIENTE', 'ACEPTADA')")
	long countCitasActivasByProfesionalAndFecha(@Param("profesionalId") Integer profesionalId,
			@Param("fechaHora") LocalDateTime fechaHora);

	@Query("SELECT c FROM Cita c LEFT JOIN FETCH c.usuario LEFT JOIN FETCH c.servicio LEFT JOIN FETCH c.profesional p LEFT JOIN FETCH p.usuario WHERE c.id = :id")
	Optional<Cita> findByIdWithAllRelations(@Param("id") Integer id);

	// MÉTODOS ADICIONALES ÚTILES para el dashboard del profesional
	@Query("SELECT c FROM Cita c WHERE c.profesional.id = :profesionalId ORDER BY c.fechaHora DESC")
	List<Cita> findCitasByProfesionalOrderByFechaDesc(@Param("profesionalId") Integer profesionalId);

	// Contar citas por estado para un profesional específico
	@Query("SELECT COUNT(c) FROM Cita c WHERE c.profesional.id = :profesionalId AND c.estado = :estado")
	long countByProfesionalIdAndEstado(@Param("profesionalId") Integer profesionalId, @Param("estado") String estado);

	boolean existsByServicioId(Integer servicioId);

}