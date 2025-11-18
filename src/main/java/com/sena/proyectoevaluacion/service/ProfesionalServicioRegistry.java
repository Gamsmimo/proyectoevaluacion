package com.sena.proyectoevaluacion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ProfesionalServicioRegistry {

	// profesionalId -> lista de id de servicios
	private Map<Integer, List<Integer>> serviciosPorProfesional = new HashMap<>();

	public void agregarServicioAProfesional(Integer profesionalId, Integer servicioId) {
		serviciosPorProfesional.computeIfAbsent(profesionalId, k -> new ArrayList<>()).add(servicioId);
	}

	public List<Integer> obtenerServiciosDeProfesional(Integer profesionalId) {
		return serviciosPorProfesional.getOrDefault(profesionalId, new ArrayList<>());
	}

	public void eliminarProfesional(Integer profesionalId) {
		serviciosPorProfesional.remove(profesionalId);
	}
}
