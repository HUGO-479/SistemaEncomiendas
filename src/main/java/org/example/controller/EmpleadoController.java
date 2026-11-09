package org.example.controller;

import org.example.agencia.repository.AgenciaRepository;
import org.example.cliente.model.Persona;
import org.example.cliente.model.TipoDocumento;
import org.example.cliente.repository.ClienteRepository;
import org.example.usuario.model.Rol;
import org.example.usuario.model.Usuario;
import org.example.usuario.repository.RolRepository;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/empleados")
public class EmpleadoController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final AgenciaRepository agenciaRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoController(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                              RolRepository rolRepository, AgenciaRepository agenciaRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.agenciaRepository = agenciaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Listar empleados
    @GetMapping
    public String listar(Model model) {
        List<Usuario> empleados = usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNombre().equals("EMPLEADO")))
                .toList();
        model.addAttribute("empleados", empleados);
        return "admin/empleados/lista";
    }

    // 2. Mostrar formulario
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("agencias", agenciaRepository.findAll());
        return "admin/empleados/formulario";
    }

    // 3. Guardar empleado
    @PostMapping("/guardar")
    public String guardar(@RequestParam("dni") String dni,
                          @RequestParam("nombres") String nombres,
                          @RequestParam("apellidos") String apellidos,
                          @RequestParam("celular") String celular,
                          @RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("agenciaId") Long agenciaId) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            return "redirect:/admin/empleados/nuevo?error=username_existe";
        }

        Optional<Persona> personaOpt = clienteRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, dni);
        Persona persona;
        if (personaOpt.isPresent()) {
            persona = personaOpt.get();
        } else {
            persona = new Persona();
            persona.setTipoDocumento(TipoDocumento.DNI);
            persona.setNumeroDocumento(dni);
            persona.setNombres(nombres);
            persona.setApellidos(apellidos);
            persona.setTelefono(celular);
            persona.setActivo(true);
            persona = clienteRepository.save(persona);
        }

        Rol rolEmpleado = rolRepository.findByNombre("EMPLEADO")
                .orElseThrow(() -> new RuntimeException("Rol EMPLEADO no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setPersona(persona);
        usuario.setAgencia(agenciaRepository.findById(agenciaId).orElse(null));
        usuario.setRoles(Set.of(rolEmpleado));
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        return "redirect:/admin/empleados";
    }
}