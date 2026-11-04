package org.example.seguimiento.controller;

import org.example.envio.model.Envio;
import org.example.envio.model.EstadoEnvio;
import org.example.envio.repository.EnvioRepository;
import org.example.seguimiento.service.SeguimientoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/seguimiento")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;
    private final EnvioRepository envioRepository;

    public SeguimientoController(SeguimientoService seguimientoService, EnvioRepository envioRepository) {
        this.seguimientoService = seguimientoService;
        this.envioRepository = envioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        // Mostramos los envíos que están en tránsito o disponibles para recojo
        List<Envio> envios = envioRepository.findAll().stream()
                .filter(e -> e.getEstado() == EstadoEnvio.EN_TRANSITO || e.getEstado() == EstadoEnvio.DISPONIBLE_RECOJO)
                .toList();
        model.addAttribute("envios", envios);
        return "empleado/seguimiento";
    }

    @PostMapping("/cambiar/{id}")
    public String cambiarEstado(@PathVariable Long id, @RequestParam("nuevoEstado") EstadoEnvio nuevoEstado) {
        seguimientoService.cambiarEstado(id, nuevoEstado);
        return "redirect:/seguimiento";
    }
}