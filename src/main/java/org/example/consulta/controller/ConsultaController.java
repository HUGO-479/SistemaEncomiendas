package org.example.consulta.controller;

import org.example.envio.model.Envio;
import org.example.envio.repository.EnvioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/consulta")
public class ConsultaController {

    private final EnvioRepository envioRepository;

    public ConsultaController(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    // 1. Mostrar el formulario de consulta pública
    @GetMapping
    public String mostrarFormulario() {
        return "consulta/index";
    }

    // 2. Procesar la búsqueda por código de guía
    @PostMapping
    public String consultar(@RequestParam("codigo") String codigo, Model model) {
        // Buscamos el envío por su número de guía
        Envio envio = envioRepository.findByNumeroGuia(codigo).orElse(null);

        if (envio == null) {
            model.addAttribute("error", "No se encontró ningún paquete con ese código.");
        } else {
            // Enviamos solo los datos no sensibles (sin DNI, sin celular)
            model.addAttribute("codigo", envio.getNumeroGuia());
            model.addAttribute("estado", envio.getEstado());
            model.addAttribute("destino", envio.getAgenciaDestino().getNombre());
            model.addAttribute("fecha", envio.getRegistradoEn());
        }

        return "consulta/index";
    }
}