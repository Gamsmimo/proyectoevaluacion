package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Cita;
import com.sena.proyectoevaluacion.repository.ICitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImplement implements ICitaService {

	@Autowired
	private ICitaRepository citaRepository;

	@Override
	public List<Cita> findAll() {
		return citaRepository.findAll();
	}

	@Override
	public Optional<Cita> findById(Integer id) {
		return citaRepository.findById(id);
	}

	@Override
	@Transactional
	public Cita save(Cita cita) {
		return citaRepository.save(cita);
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {
		citaRepository.deleteById(id);
	}

	@Override
	public List<Cita> findByUsuarioId(Integer usuarioId) {
		return citaRepository.findByUsuarioId(usuarioId);
	}

	@Override
	public List<Cita> findByProfesionalId(Integer profesionalId) {
		return citaRepository.findByProfesionalId(profesionalId);
	}

	@Override
	public List<Cita> findByServicioId(Integer servicioId) {
		return citaRepository.findByServicioId(servicioId);
	}

	@Override
	public List<Cita> findByEstado(String estado) {
		return citaRepository.findByEstado(estado);
	}

	@Override
	public List<Cita> findByProfesionalIdAndEstado(Integer profesionalId, String estado) {
		return citaRepository.findByProfesionalIdAndEstado(profesionalId, estado);
	}

	@Override
	public boolean verificarDisponibilidad(Integer profesionalId, LocalDateTime fechaHora) {
		try {
			// Buscar citas existentes para el profesional en la misma fecha y hora
			List<Cita> citasExistentes = citaRepository.findByProfesionalIdAndFechaHora(profesionalId, fechaHora);

			// Si no hay citas existentes, está disponible
			boolean disponible = citasExistentes.isEmpty();

			System.out.println("DEBUG - Verificando disponibilidad:");
			System.out.println("Profesional ID: " + profesionalId);
			System.out.println("Fecha/Hora: " + fechaHora);
			System.out.println("Citas existentes: " + citasExistentes.size());
			System.out.println("Disponible: " + disponible);

			return disponible;

		} catch (Exception e) {
			System.err.println("Error al verificar disponibilidad: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// Mantener el método con String por compatibilidad (si es necesario)
	@Override
	public boolean verificarDisponibilidad(Integer profesionalId, String fechaHora) {
		try {
			// Convertir String a LocalDateTime
			LocalDateTime fechaHoraLocal = parseFechaHoraString(fechaHora);
			return verificarDisponibilidad(profesionalId, fechaHoraLocal);
		} catch (Exception e) {
			System.err.println("Error al convertir fecha String: " + e.getMessage());
			return false;
		}
	}

	@Override
	public Optional<Cita> findByIdWithAllRelations(Integer id) {
		return citaRepository.findByIdWithAllRelations(id);
	}

	// Método auxiliar para convertir String a LocalDateTime
	private LocalDateTime parseFechaHoraString(String fechaHoraStr) {
		try {
			// Si viene en formato "yyyy-MM-dd HH:mm:ss"
			if (fechaHoraStr.contains(" ") && fechaHoraStr.contains(":")) {
				// Reemplazar espacio por T para formato ISO
				String isoFormat = fechaHoraStr.replace(" ", "T");
				return LocalDateTime.parse(isoFormat);
			}
			// Si ya viene en formato ISO
			else if (fechaHoraStr.contains("T")) {
				return LocalDateTime.parse(fechaHoraStr);
			}
			// Formato por defecto
			else {
				return LocalDateTime.parse(fechaHoraStr);
			}
		} catch (Exception e) {
			System.err.println("Error al parsear fecha: " + fechaHoraStr + " - " + e.getMessage());
			return LocalDateTime.now();
		}
	}
}