package com.sena.proyectoevaluacion.repository;

import com.sena.proyectoevaluacion.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IServicioRepository extends JpaRepository<Servicio, Integer> {
	// Mantener solo búsqueda por nombre
	List<Servicio> findByNombreContainingIgnoreCase(String nombre);
}