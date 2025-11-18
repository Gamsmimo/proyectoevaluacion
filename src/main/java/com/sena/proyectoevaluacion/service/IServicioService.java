package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Servicio;
import java.util.List;
import java.util.Optional;

public interface IServicioService {
	List<Servicio> findAll();

	Optional<Servicio> findById(Integer id);

	Servicio save(Servicio servicio);

	void deleteById(Integer id);

	// Mantener solo búsqueda por nombre
	List<Servicio> findByNombreContainingIgnoreCase(String nombre);
}