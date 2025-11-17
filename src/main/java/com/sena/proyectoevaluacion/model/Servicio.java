package com.sena.proyectoevaluacion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servicio")
public class Servicio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	// CORREGIR: Usar el nombre exacto de la columna en la BD
	@Column(name = "descripción", length = 255)
	private String descripcion;

	// CORREGIR: Usar el nombre exacto de la columna en la BD
	@Column(name = "duración")
	private String duracion;

	@Column(name = "precio")
	private Double precio;

	// Constructores
	public Servicio() {
	}

	public Servicio(String nombre, String descripcion, String duracion, Double precio) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.duracion = duracion;
		this.precio = precio;
	}

	// Getters y Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDuracion() {
		return duracion;
	}

	public void setDuracion(String duracion) {
		this.duracion = duracion;
	}

	public Double getPrecio() {
		return precio;
	}

	public void setPrecio(Double precio) {
		this.precio = precio;
	}

	// MÉTODOS DE COMPATIBILIDAD AGREGADOS:
	public Double getValor() {
		return this.precio;
	}

	public String getTiempoEstimado() {
		return this.duracion;
	}

	@Override
	public String toString() {
		return "Servicio{" + "id=" + id + ", nombre='" + nombre + '\'' + ", descripcion='" + descripcion + '\''
				+ ", duracion='" + duracion + '\'' + ", precio=" + precio + '}';
	}
}