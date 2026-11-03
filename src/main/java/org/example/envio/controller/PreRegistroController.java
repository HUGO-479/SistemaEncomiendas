package org.example.envio.controller;

import org.example.agencia.service.AgenciaService;
import org.example.envio.dto.PreRegistroDTO;
import org.example.envio.model.Envio;
import org.example.envio.service.EnvioService;
import org.example.tipopaquete.repository.TipoPaqueteRepository;
import org.example.usuario.model.Usuario;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente/pre-registro")
public class PreRegistroController {

    private final AgenciaService agenciaService;
    private final TipoPaqueteRepository tipoPaqueteRepository;
    private final EnvioService envioService;
    private final UsuarioRepository usuarioRepository;

    public PreRegistroController(AgenciaService agenciaService, TipoPaqueteRepository tipoPaqueteRepository, EnvioService envioService, UsuarioRepository usuarioRepository) {
        this.agenciaService = agenciaService;
        this.tipoPaqueteRepository = tipoPaqueteRepository;
        this.envioService = envioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("agencias", agenciaService.listarTodas());
        model.addAttribute("tiposPaquete", tipoPaqueteRepository.findAll());
        model.addAttribute("preRegistro", new PreRegistroDTO());
        return "cliente/pre-registro";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute PreRegistroDTO dto, Authentication auth, RedirectAttributes redirect) {
        try {
            Usuario cliente = usuarioRepository.findByUsername(auth.getName()).orElseThrow();
            Envio env = envioService.registrarPreRegistro(dto, cliente);

            redirect.addFlashAttribute("mensajeExito", "Pre-registro exitoso. Su código es: " + env.getNumeroGuia());

            // ✅ Redirigimos al Dashboard (no al formulario)
            return "redirect:/cliente/dashboard";
        } catch (Exception e) {
            redirect.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/cliente/pre-registro";
        }
    }

}