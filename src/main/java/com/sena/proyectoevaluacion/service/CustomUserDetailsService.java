package com.sena.proyectoevaluacion.service;

import com.sena.proyectoevaluacion.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioService usuarioService;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		System.out.println("DEBUG: Buscando usuario por email: " + email);

		Optional<Usuario> usuarioOpt = usuarioService.findByEmail(email);

		if (usuarioOpt.isEmpty()) {
			throw new UsernameNotFoundException("Usuario no encontrado: " + email);
		}

		Usuario usuario = usuarioOpt.get();
		System.out.println("DEBUG: Usuario encontrado - ID: " + usuario.getId() + ", Nombre: " + usuario.getNombre());

		// ✅ VERIFICAR SI ES PROFESIONAL
		List<GrantedAuthority> authorities = new ArrayList<>();

		if (usuario.getProfesional() != null) {
			System.out.println("DEBUG: ✅ Usuario " + email + " es PROFESIONAL");
			authorities.add(new SimpleGrantedAuthority("ROLE_PROFESIONAL"));
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		} else {
			System.out.println("DEBUG: 🔵 Usuario " + email + " es USER normal");
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		return new User(usuario.getEmail(), usuario.getPassword(), true, true, true, true, authorities);
	}
}