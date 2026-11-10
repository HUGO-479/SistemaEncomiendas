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
    public String buscar(@RequestParam("busqueda") String busqueda, Model model, Authentication auth) {
        try {
            // 1. Obtener la agencia del empleado logueado
            Usuario empleado = usuarioRepository.findByUsername(auth.getName()).orElseThrow();
            Agencia agenciaEmpleado = empleado.getAgencia();

            // 2. Pasarle la agencia al servicio para filtrar
            List<Envio> envios = recepcionService.buscarPendientes(busqueda, agenciaEmpleado);

            if (envios.isEmpty()) {
                model.addAttribute("error", "No se encontraron envíos pendientes con ese dato (o el paquete ya se encuentra en su agencia).");
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
    public String mostrarRegistroDirecto(Model model, Authentication auth) {
        Usuario empleado = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        Agencia agenciaOrigen = empleado.getAgencia();

        if (agenciaOrigen == null) {
            throw new RuntimeException("El empleado no tiene una agencia asignada.");
        }

        List<Agencia> agenciasDisponibles = agenciaRepository.findByIdNot(agenciaOrigen.getId());

        model.addAttribute("agencias", agenciasDisponibles);
        model.addAttribute("tiposPaquete", tipoPaqueteRepository.findAll());
        model.addAttribute("agenciaOrigen", agenciaOrigen);

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
            @RequestParam("tipoPaqueteId") Long tipoPaqueteId,
            @RequestParam("descripcionBulto") String descripcionBulto,
            Authentication auth) {

        Usuario empleado = usuarioRepository.findByUsername(auth.getName()).orElseThrow();
        Agencia agenciaOrigen = empleado.getAgencia();

        Envio envio = recepcionService.registrarDirectoEnAgencia(
                remitenteTipoDoc, remitenteDni, remitenteNombres, remitenteApellidos, remitenteRazonSocial, remitenteCelular,
                destinatarioTipoDoc, destinatarioDni, destinatarioNombres, destinatarioApellidos, destinatarioRazonSocial, destinatarioCelular,
                agenciaDestinoId, tipoPaqueteId, descripcionBulto, agenciaOrigen
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

        recepcionService.confirmarRecepcion(id, peso, largo, ancho, alto);

        redirect.addFlashAttribute("mensajeExito", "Recepción exitosa. El paquete ya está en agencia.");
        redirect.addFlashAttribute("envioId", id);

        return "redirect:/recepcion/exito";
    }

    // ==========================================
    // FLUJO 4: PANTALLA DE ÉXITO
    // ==========================================
    @GetMapping("/exito")
    public String mostrarExito() {
        return "empleado/exito";
    }
}