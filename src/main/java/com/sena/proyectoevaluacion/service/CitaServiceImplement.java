package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Cita;
import com.sena.proyectoevaluacion.repository.ICitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImplement implements ICitaService {

	@Autowired
	private ICitaRepository citaRepository;

	@Override
	public List<Cita> findAll() {
		try {
			return citaRepository.findAll();
		} catch (Exception e) {
			System.err.println("Error en findAll: " + e.getMessage());
			return new ArrayList<>();
		}
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
		try {
			List<Cita> citas = citaRepository.findByUsuarioId(usuarioId);

			System.out.println("=== CITAS POR USUARIO ===");
			System.out.println("Usuario ID: " + usuarioId);
			System.out.println("Citas encontradas: " + citas.size());

			return citas;
		} catch (Exception e) {
			System.err.println("Error en findByUsuarioId: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	@Override
	public List<Cita> findByProfesionalId(Integer profesionalId) {
		try {
			return citaRepository.findByProfesionalId(profesionalId);
		} catch (Exception e) {
			System.err.println("Error en findByProfesionalId: " + e.getMessage());
			return new ArrayList<>();
		}
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
		try {
			List<Cita> citas = citaRepository.findByProfesionalIdAndEstado(profesionalId, estado);

			System.out.println("=== CITAS POR PROFESIONAL Y ESTADO ===");
			System.out.println("Profesional ID: " + profesionalId);
			System.out.println("Estado: " + estado);
			System.out.println("Citas encontradas: " + citas.size());

			return citas;
		} catch (Exception e) {
			System.err.println("Error en findByProfesionalIdAndEstado: " + e.getMessage());
			System.out.println("Profesional ID: " + profesionalId + ", Estado: " + estado);
			e.printStackTrace();

			// Fallback: filtrar manualmente si el método del repository falla
			return filtrarCitasManual(profesionalId, estado);
		}
	}

	// Método de respaldo en caso de que el repository falle
	private List<Cita> filtrarCitasManual(Integer profesionalId, String estado) {
		try {
			List<Cita> todasLasCitas = citaRepository.findByProfesionalId(profesionalId);
			List<Cita> citasFiltradas = new ArrayList<>();

			for (Cita cita : todasLasCitas) {
				if (estado.equals(cita.getEstado())) {
					citasFiltradas.add(cita);
				}
			}

			System.out.println("Método manual - Citas filtradas: " + citasFiltradas.size());
			return citasFiltradas;

		} catch (Exception e) {
			System.err.println("Error en método manual: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	@Override
	public boolean verificarDisponibilidad(Integer profesionalId, LocalDateTime fechaHora) {
		try {
			List<Cita> citasExistentes = citaRepository.findByProfesionalIdAndFechaHora(profesionalId, fechaHora);
			boolean disponible = citasExistentes.isEmpty();

			System.out.println("=== VERIFICACIÓN DISPONIBILIDAD ===");
			System.out.println("Profesional ID: " + profesionalId);
			System.out.println("Fecha/Hora: " + fechaHora);
			System.out.println("Citas existentes: " + citasExistentes.size());
			System.out.println("Disponible: " + disponible);

			return disponible;

		} catch (Exception e) {
			System.err.println("Error al verificar disponibilidad: " + e.getMessage());
			return false;
		}
	}

	@Override
	public Optional<Cita> findByIdWithAllRelations(Integer id) {
		return citaRepository.findByIdWithAllRelations(id);
	}

	private LocalDateTime parseFechaHoraString(String fechaHoraStr) {
		try {
			if (fechaHoraStr.contains(" ") && fechaHoraStr.contains(":")) {
				String isoFormat = fechaHoraStr.replace(" ", "T");
				return LocalDateTime.parse(isoFormat);
			} else if (fechaHoraStr.contains("T")) {
				return LocalDateTime.parse(fechaHoraStr);
			} else {
				return LocalDateTime.parse(fechaHoraStr);
			}
		} catch (Exception e) {
			System.err.println("Error al parsear fecha: " + fechaHoraStr + " - " + e.getMessage());
			return LocalDateTime.now();
		}
	}

}