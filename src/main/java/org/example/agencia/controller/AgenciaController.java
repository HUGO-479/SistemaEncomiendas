package org.example.agencia.controller;

import org.example.agencia.model.Agencia;
import org.example.agencia.service.AgenciaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/agencias")
public class AgenciaController {

    private final AgenciaService service;

    public AgenciaController(AgenciaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("agencias", service.listarTodas());
        return "admin/agencias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("agencia", new Agencia());
        return "admin/agencias/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("agencia", service.buscarPorId(id));
        return "admin/agencias/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Agencia agencia,
            RedirectAttributes redirect) {

        try {
            service.guardar(agencia);
            redirect.addFlashAttribute(
                    "exito", "Agencia guardada correctamente");
            return "redirect:/admin/agencias";

        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/agencias";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        service.cambiarEstado(id);

        redirect.addFlashAttribute(
                "exito", "Estado actualizado correctamente");

        return "redirect:/admin/agencias";
    }
}