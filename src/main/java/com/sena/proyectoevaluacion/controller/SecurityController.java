package com.sena.proyectoevaluacion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityController {

	@GetMapping("/acceso-denegado")
	public String accesoDenegado(Model model) {
		model.addAttribute("error", "No tienes permisos para acceder a esta página");
		return "error/acceso-denegado";
	}
}