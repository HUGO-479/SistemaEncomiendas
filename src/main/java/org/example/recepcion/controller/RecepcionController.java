package org.example.recepcion.controller;

import org.example.agencia.model.Agencia;
import org.example.agencia.repository.AgenciaRepository;
import org.example.envio.model.Envio;
import org.example.cliente.model.TipoDocumento;
import org.example.recepcion.service.RecepcionService;
import org.example.tipopaquete.repository.TipoPaqueteRepository;
import org.example.usuario.model.Usuario;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/recepcion")
public class RecepcionController {

    private final RecepcionService recepcionService;
    private final AgenciaRepository agenciaRepository;
    private final TipoPaqueteRepository tipoPaqueteRepository;
    private final UsuarioRepository usuarioRepository;

    public RecepcionController(RecepcionService recepcionService,
                               AgenciaRepository agenciaRepository,
                               TipoPaqueteRepository tipoPaqueteRepository,
                               UsuarioRepository usuarioRepository) {
        this.recepcionService = recepcionService;
        this.agenciaRepository = agenciaRepository;
        this.tipoPaqueteRepository = tipoPaqueteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // FLUJO 1: BUSCAR PRE-REGISTRO
    // ==========================================
    @GetMapping("/buscar")
    public String mostrarBuscador() {
        return "empleado/buscar";
    }

    @PostMapping("/buscar")
    public String buscar(@RequestParam("busqueda") String busqueda, Model model) {
        try {
            List<Envio> envios = recepcionService.buscarPendientes(busqueda);
            if (envios.isEmpty()) {
                model.addAttribute("error", "No se encontraron envíos pendientes con ese dato.");
            } else {
                model.addAttribute("envios", envios);
            }
            model.addAttribute("busqueda", busqueda);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "empleado/buscar";
    }

    // ==========================================
    // FLUJO 2: REGISTRO DIRECTO
    // ==========================================
    @GetMapping("/registro-directo")
    public String mostrarRegistroDirecto(Model model) {
        model.addAttribute("agencias", agenciaRepository.findAll());
        model.addAttribute("tiposPaquete", tipoPaqueteRepository.findAll());
        return "empleado/registro-directo";
    }

    @PostMapping("/registro-directo")
    public String guardarRegistroDirecto(
            @RequestParam("remitenteTipoDocumento") TipoDocumento remitenteTipoDoc,
            @RequestParam("remitenteDni") String remitenteDni,
            @RequestParam("remitenteNombres") String remitenteNombres,
            @RequestParam("remitenteApellidos") String remitenteApellidos,
            @RequestParam("remitenteRazonSocial") String remitenteRazonSocial,
            @RequestParam("remitenteCelular") String remitenteCelular,
            @RequestParam("destinatarioTipoDocumento") TipoDocumento destinatarioTipoDoc,
            @RequestParam("destinatarioDni") String destinatarioDni,
            @RequestParam("destinatarioNombres") String destinatarioNombres,
            @RequestParam("destinatarioApellidos") String destinatarioApellidos,
            @RequestParam("destinatarioRazonSocial") String destinatarioRazonSocial,
            @RequestParam("destinatarioCelular") String destinatarioCelular,
            @RequestParam("agenciaDestinoId") Long agenciaDestinoId,
            @RequestParam("descripcionBulto") String descripcionBulto,
            Authentication auth) {

        Usuario empleado = usuarioRepository.findByUsername(auth.getName()).orElseThrow();
        Agencia agenciaOrigen = empleado.getAgencia();

        Envio envio = recepcionService.registrarDirectoEnAgencia(
                remitenteTipoDoc, remitenteDni, remitenteNombres, remitenteApellidos, remitenteRazonSocial, remitenteCelular,
                destinatarioTipoDoc, destinatarioDni, destinatarioNombres, destinatarioApellidos, destinatarioRazonSocial, destinatarioCelular,
                agenciaDestinoId, descripcionBulto, agenciaOrigen
        );

        return "redirect:/recepcion/pesar/" + envio.getId();
    }

    // ==========================================
    // FLUJO 3: PESAJE
    // ==========================================
    @GetMapping("/pesar/{id}")
    public String mostrarFormularioPesar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("envioId", id);
        return "empleado/pesar";
    }

    @PostMapping("/pesar/{id}")
    public String pesar(@PathVariable("id") Long id,
                        @RequestParam("peso") Double peso,
                        @RequestParam("largo") Double largo,
                        @RequestParam("ancho") Double ancho,
                        @RequestParam("alto") Double alto,
                        RedirectAttributes redirect) {

        // 1. Confirmar la recepción y calcular precio
        recepcionService.confirmarRecepcion(id, peso, largo, ancho, alto);

        // 2. Enviar mensaje de éxito y ID
        redirect.addFlashAttribute("mensajeExito", "Recepción exitosa. El paquete ya está en tránsito.");
        redirect.addFlashAttribute("envioId", id);

        // 3. Redirigir a la vista de éxito
        return "redirect:/recepcion/exito";
    }

    // ==========================================
    // FLUJO 4: PANTALLA DE ÉXITO (Ticket y Guía)
    // ==========================================
    @GetMapping("/exito")
    public String mostrarExito() {
        return "empleado/exito";
    }
}