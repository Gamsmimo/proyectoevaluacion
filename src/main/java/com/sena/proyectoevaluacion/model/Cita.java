package com.sena.proyectoevaluacion.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
public class Cita {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "fecha_hora")
	private LocalDateTime fechaHora;

	@Column(name = "estado")
	private String estado;

	// Relación: Una cita pertenece a un usuario
	@ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER
	@JoinColumn(name = "usuario_id")
	@JsonIgnore
	private Usuario usuario;

	// Relación: Una cita tiene un servicio
	@ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER
	@JoinColumn(name = "servicio_id")
	private Servicio servicio;

	// Relación: Una cita tiene un profesional
	@ManyToOne(fetch = FetchType.EAGER) // Cambiado a EAGER
	@JoinColumn(name = "profesional_id")
	private Profesional profesional;

	// Constructores, getters y setters... (mantén todo igual)

	public Cita() {
	}

	public Cita(LocalDateTime fechaHora, String estado, Usuario usuario, Servicio servicio, Profesional profesional) {
		this.fechaHora = fechaHora;
		this.estado = estado;
		this.usuario = usuario;
		this.servicio = servicio;
		this.profesional = profesional;
	}

	// Getters y Setters (mantén todo igual)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Servicio getServicio() {
		return servicio;
	}

	public void setServicio(Servicio servicio) {
		this.servicio = servicio;
	}

	public Profesional getProfesional() {
		return profesional;
	}

	public void setProfesional(Profesional profesional) {
		this.profesional = profesional;
	}

	@Override
	public String toString() {
		return "Cita{" + "id=" + id + ", fechaHora=" + fechaHora + ", estado='" + estado + '\'' + '}';
	}
}