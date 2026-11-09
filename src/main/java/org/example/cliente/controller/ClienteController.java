package org.example.cliente.controller;

import org.example.cliente.service.PersonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final PersonaService personaService;

    public ClienteController(PersonaService personaService) {
        this.personaService = personaService;
    }

    // Ruta para el panel del cliente
    @GetMapping("/dashboard")
    public String dashboard() {
        return "cliente/dashboard"; // Busca el HTML en templates/cliente/dashboard.html
    }

    // Ruta que ya tenías para listar clientes
    @GetMapping
    public String listar(Model model) {
        // model.addAttribute("clientes", clienteService.listarTodos());
        return "cliente/lista";
    }
}