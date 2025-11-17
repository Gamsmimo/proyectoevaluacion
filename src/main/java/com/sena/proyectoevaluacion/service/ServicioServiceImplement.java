package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.repository.IServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioServiceImplement implements IServicioService {

	@Autowired
	private IServicioRepository servicioRepository;

	@Override
	public List<Servicio> findAll() {
		return servicioRepository.findAll();
	}

	@Override
	public Optional<Servicio> findById(Integer id) {
		return servicioRepository.findById(id);
	}

	@Override
	@Transactional
	public Servicio save(Servicio servicio) {
		return servicioRepository.save(servicio);
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {
		servicioRepository.deleteById(id);
	}


	@Override
	public List<Servicio> findByNombreContainingIgnoreCase(String nombre) {
		return servicioRepository.findByNombreContainingIgnoreCase(nombre);
	}
}