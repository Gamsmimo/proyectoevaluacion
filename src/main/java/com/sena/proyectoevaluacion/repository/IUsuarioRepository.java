package com.sena.proyectoevaluacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sena.proyectoevaluacion.model.Usuario;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {

	// Para autenticación y verificación
	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<Usuario> findByEmailAndPassword(String email, String password);

	// Para búsquedas
	List<Usuario> findByNombreContainingIgnoreCase(String nombre);

	List<Usuario> findByTelefono(String telefono);

	// Para reportes y estadísticas
	@Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaRegistro >= CURRENT_DATE")
	long countUsuariosRegistradosHoy();
}