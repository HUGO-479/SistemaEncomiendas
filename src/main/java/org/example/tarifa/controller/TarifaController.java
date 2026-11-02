
package org.example.tarifa.controller;

import org.example.agencia.service.AgenciaService;
import org.example.tarifa.model.Tarifa;
import org.example.tarifa.service.TarifaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/tarifas")
public class TarifaController {

    private final TarifaService service;
    private final AgenciaService agenciaService;

    public TarifaController(
            TarifaService service,
            AgenciaService agenciaService) {
        this.service = service;
        this.agenciaService = agenciaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tarifas", service.listarTodas());
        return "admin/tarifas/lista";
    }

    private void cargarFormulario(Model model, Tarifa tarifa) {
        model.addAttribute("tarifa", tarifa);
        model.addAttribute("agencias", agenciaService.listarTodas());
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        cargarFormulario(model, new Tarifa());
        return "admin/tarifas/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        cargarFormulario(model, service.buscarPorId(id));
        return "admin/tarifas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam(required = false) Long id,
            @RequestParam Long origenId,
            @RequestParam Long destinoId,
            @RequestParam BigDecimal precioBase,
            @RequestParam BigDecimal precioPorKg,
            Model model,
            RedirectAttributes redirect) {

        try {
            service.guardar(
                    id, origenId, destinoId,
                    precioBase, precioPorKg);

            redirect.addFlashAttribute(
                    "exito", "Tarifa guardada correctamente");

            return "redirect:/admin/tarifas";

        } catch (IllegalArgumentException e) {
            Tarifa datos = new Tarifa();
            datos.setId(id);
            datos.setPrecioBase(precioBase);
            datos.setPrecioPorKg(precioPorKg);

            // Conserva la selección de agencias
            // cuando existen errores de validación.
            org.example.agencia.model.Agencia origen =
                    new org.example.agencia.model.Agencia();
            origen.setId(origenId);
            datos.setAgenciaOrigen(origen);

            org.example.agencia.model.Agencia destino =
                    new org.example.agencia.model.Agencia();
            destino.setId(destinoId);
            datos.setAgenciaDestino(destino);

            cargarFormulario(model, datos);
            model.addAttribute("error", e.getMessage());

            return "admin/tarifas/formulario";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        service.cambiarEstado(id);
        redirect.addFlashAttribute(
                "exito", "Estado actualizado correctamente");

        return "redirect:/admin/tarifas";
    }
}
