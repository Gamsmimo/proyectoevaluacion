package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.repository.IServicioRepository;
import com.sena.proyectoevaluacion.repository.ICitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioServiceImplement implements IServicioService {

	@Autowired
	private IServicioRepository servicioRepository;

	@Autowired
	private ICitaRepository citaRepository; // <-- FALTABA ESTO

	@Override
	public boolean tieneCitasAsociadas(Integer idServicio) {
		return citaRepository.existsByServicioId(idServicio);
	}

	@Override
	public List<Servicio> findAll() {
		try {
			return servicioRepository.findAll();
		} catch (Exception e) {
			System.err.println("Error en ServicioService.findAll: " + e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Override
	public Optional<Servicio> findById(Integer id) {
		try {
			return servicioRepository.findById(id);
		} catch (Exception e) {
			System.err.println("Error en ServicioService.findById: " + e.getMessage());
			e.printStackTrace();
			return Optional.empty();
		}
	}

	@Override
	@Transactional
	public Servicio save(Servicio servicio) {
		try {
			return servicioRepository.save(servicio);
		} catch (Exception e) {
			System.err.println("Error en ServicioService.save: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {
		try {
			servicioRepository.deleteById(id);
		} catch (Exception e) {
			System.err.println("Error en ServicioService.deleteById: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public List<Servicio> findByNombreContainingIgnoreCase(String nombre) {
		try {
			return servicioRepository.findByNombreContainingIgnoreCase(nombre);
		} catch (Exception e) {
			System.err.println("Error en ServicioService.findByNombre: " + e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

}
