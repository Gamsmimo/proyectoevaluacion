// Profesional.java
package com.sena.proyectoevaluacion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "profesional")
public class Profesional {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String especialidad;

	@Column(name = "horario_disponible")
	private String horarioDisponible;

	@Column(name = "usuario_id")
	private Integer usuarioId;

	// Constructor por defecto
	public Profesional() {
	}

	// Constructor con parámetros
	public Profesional(String especialidad, String horarioDisponible, Integer usuarioId) {
		this.especialidad = especialidad;
		this.horarioDisponible = horarioDisponible;
		this.usuarioId = usuarioId;
	}

	// Getters y Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEspecialidad() {
		return especialidad;
	}

	public void setEspecialidad(String especialidad) {
		this.especialidad = especialidad;
	}

	public String getHorarioDisponible() {
		return horarioDisponible;
	}

	public void setHorarioDisponible(String horarioDisponible) {
		this.horarioDisponible = horarioDisponible;
	}

	public Integer getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Integer usuarioId) {
		this.usuarioId = usuarioId;
	}
}