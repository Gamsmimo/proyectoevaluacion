package com.sena.proyectoevaluacion.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "profesional")
public class Profesional {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String especialidad;

	@Column(name = "horario_disponible")
	private LocalDateTime horarioDisponible; // Cambiar de String a LocalDateTime

	// Relación: Un profesional pertenece a un usuario
	@OneToOne(fetch = FetchType.EAGER) // Cambiado a EAGER para cargar automáticamente
	@JoinColumn(name = "usuario_id")
	@JsonIgnore
	private Usuario usuario;

	// Relación: Un profesional puede tener múltiples citas
	@OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Cita> citas;

	// Constructor por defecto
	public Profesional() {
	}

	// Constructor con parámetros
	public Profesional(String especialidad, LocalDateTime horarioDisponible, Usuario usuario) {
		this.especialidad = especialidad;
		this.horarioDisponible = horarioDisponible;
		this.usuario = usuario;
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

	public LocalDateTime getHorarioDisponible() {
		return horarioDisponible;
	}

	public void setHorarioDisponible(LocalDateTime horarioDisponible) {
		this.horarioDisponible = horarioDisponible;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Cita> getCitas() {
		return citas;
	}

	public void setCitas(List<Cita> citas) {
		this.citas = citas;
	}

	// MÉTODO AGREGADO: Obtener el nombre del usuario relacionado
	public String getNombre() {
		if (this.usuario != null) {
			return this.usuario.getNombre();
		}
		return "Profesional no asignado";
	}

	@Override
	public String toString() {
		return "Profesional{" + "id=" + id + ", especialidad='" + especialidad + '\'' + ", horarioDisponible="
				+ horarioDisponible + ", usuario=" + (usuario != null ? usuario.getNombre() : "null") + '}';
	}
}