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

    @GetMapping("/pesar/{id}")
    public String mostrarFormularioPesar(@PathVariable("id") Long id, Model model) {
        // Solo pasamos el ID al HTML
        model.addAttribute("envioId", id);
        return "empleado/pesar";
    }

    // 1. Mostrar formulario de búsqueda
    @GetMapping("/buscar")
    public String mostrarBuscador() {
        return "empleado/buscar";
    }

    // 2. Procesar la búsqueda (DNI, RUC, Pasaporte o Guía)
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

    // 3. Mostrar formulario de Registro Directo
    @GetMapping("/registro-directo")
    public String mostrarRegistroDirecto(Model model) {
        model.addAttribute("agencias", agenciaRepository.findAll());
        model.addAttribute("tiposPaquete", tipoPaqueteRepository.findAll());
        return "empleado/registro-directo";
    }

    // 4. Procesar el Registro Directo
    @PostMapping("/registro-directo")
    public String guardarRegistroDirecto(
            @RequestParam("remitenteTipoDocumento") TipoDocumento remitenteTipoDocumento,
            @RequestParam("remitenteDni") String remitenteDni,
            @RequestParam("remitenteNombres") String remitenteNombres,
            @RequestParam("remitenteApellidos") String remitenteApellidos,
            @RequestParam("remitenteRazonSocial") String remitenteRazonSocial,
            @RequestParam("remitenteCelular") String remitenteCelular,
            @RequestParam("destinatarioTipoDocumento") TipoDocumento destinatarioTipoDocumento,
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
                remitenteTipoDocumento, remitenteDni, remitenteNombres, remitenteApellidos, remitenteRazonSocial, remitenteCelular,
                destinatarioTipoDocumento, destinatarioDni, destinatarioNombres, destinatarioApellidos, destinatarioRazonSocial, destinatarioCelular,
                agenciaDestinoId, descripcionBulto, agenciaOrigen
        );

        return "redirect:/recepcion/pesar/" + envio.getId();
    }


    // 6. Procesar el pesaje
    @PostMapping("/pesar/{id}")
    public String pesar(@PathVariable("id") Long id,
                        @RequestParam("peso") Double peso,
                        @RequestParam("largo") Double largo,
                        @RequestParam("ancho") Double ancho,
                        @RequestParam("alto") Double alto,
                        Model model) {

        recepcionService.confirmarRecepcion(id, peso, largo, ancho, alto);

        // Enviamos el ID al HTML de éxito
        model.addAttribute("envioId", id);
        return "empleado/exito"; // ✅ Nueva vista
    }
}