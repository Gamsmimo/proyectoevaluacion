package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Cita;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ICitaService {
	List<Cita> findAll();

	Optional<Cita> findById(Integer id);

	Cita save(Cita cita);

	void deleteById(Integer id);

	List<Cita> findByUsuarioId(Integer usuarioId);

	List<Cita> findByProfesionalId(Integer profesionalId);

	List<Cita> findByServicioId(Integer servicioId);

	List<Cita> findByEstado(String estado);

	List<Cita> findByProfesionalIdAndEstado(Integer profesionalId, String estado);

	// Método principal con LocalDateTime
	boolean verificarDisponibilidad(Integer profesionalId, LocalDateTime fechaHora);

	// Método sobrecargado con String para compatibilidad
	boolean verificarDisponibilidad(Integer profesionalId, String fechaHora);

	Optional<Cita> findByIdWithAllRelations(Integer id);
}