package com.sena.proyectoevaluacion.controller;

import com.sena.proyectoevaluacion.model.Servicio;
import com.sena.proyectoevaluacion.service.IServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

	@Autowired
	private IServicioService servicioService;

	@GetMapping("/listar")
	public String listarServicios(Model model) {
		try {
			List<Servicio> servicios = servicioService.findAll();
			model.addAttribute("servicios", servicios);
			return "servicios/listar";
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar servicios: " + e.getMessage());
			return "servicios/listar";
		}
	}
}