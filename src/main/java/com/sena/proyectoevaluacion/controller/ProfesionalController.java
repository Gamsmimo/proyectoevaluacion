// ProfesionalController.java
package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Profesional;
import com.sena.proyectoevaluacion.model.Usuario;
import com.sena.proyectoevaluacion.service.IProfesionalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/profesional")
public class ProfesionalController {

	@Autowired
	private IProfesionalService profesionalService;

	// Vista del dashboard del profesional
	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		Optional<Profesional> profesionalOpt = profesionalService.findByUsuarioId(usuario.getId());
		if (profesionalOpt.isEmpty()) {
			return "redirect:/usuarios/home";
		}

		model.addAttribute("usuario", usuario);
		model.addAttribute("profesional", profesionalOpt.get());
		return "Profesional/profesional"; // Asegúrate que coincida con el nombre del archivo
	}

	// Vista para registro de profesional (solo para usuarios logueados)
	@GetMapping("/registro")
	public String registroProfesional(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		// Verificar si ya es profesional
		if (profesionalService.existsByUsuarioId(usuario.getId())) {
			return "redirect:/profesional/dashboard";
		}

		model.addAttribute("usuario", usuario);
		return "Profesional/registro";
	}

	// Procesar registro de profesional
	@PostMapping("/registro")
	public String procesarRegistroProfesional(@RequestParam String especialidad, @RequestParam String horarioDisponible,
			HttpSession session, RedirectAttributes redirectAttributes) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/usuarios/login";
		}

		try {
			// Crear nuevo profesional
			Profesional profesional = new Profesional(especialidad, horarioDisponible, usuario.getId());
			profesionalService.save(profesional);

			redirectAttributes.addFlashAttribute("success", "¡Registro como profesional exitoso!");
			return "redirect:/profesional/dashboard";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error en el registro: " + e.getMessage());
			return "redirect:/profesional/registro";
		}
	}

	// API REST para profesionales
	@GetMapping("/api")
	@ResponseBody
	public java.util.List<Profesional> listarProfesionales() {
		return profesionalService.findAll();
	}

	@GetMapping("/api/{id}")
	@ResponseBody
	public Profesional obtenerProfesional(@PathVariable Integer id) {
		return profesionalService.findById(id).orElse(null);
	}
}
