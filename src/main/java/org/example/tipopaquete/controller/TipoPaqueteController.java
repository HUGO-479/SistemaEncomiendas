package org.example.tipopaquete.controller;

import org.example.tipopaquete.model.TipoPaquete;
import org.example.tipopaquete.service.TipoPaqueteService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/tipos-paquete")
public class TipoPaqueteController {

    private final TipoPaqueteService service;

    public TipoPaqueteController(TipoPaqueteService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", service.listarTodos());
        return "admin/tipopaquete/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("tipo", new TipoPaquete());
        return "admin/tipopaquete/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("tipo", service.buscarPorId(id));

        return "admin/tipopaquete/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute("tipo") TipoPaquete tipo,
            Model model,
            RedirectAttributes redirect) {

        try {
            service.guardar(tipo);

            redirect.addFlashAttribute(
                    "exito",
                    "Tipo de paquete guardado correctamente"
            );

            return "redirect:/admin/tipos-paquete";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            return "admin/tipopaquete/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        try {
            service.cambiarEstado(id);

            redirect.addFlashAttribute(
                    "exito",
                    "Estado actualizado correctamente"
            );

        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/tipos-paquete";
    }
}