package org.example;

import org.example.persona.model.Persona;
import org.example.persona.model.TipoDocumento;
import org.example.persona.repository.PersonaRepository;
import org.example.usuario.model.Rol;
import org.example.usuario.model.Usuario;
import org.example.usuario.repository.RolRepository;
import org.example.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String EMPLEADO_USERNAME = "empleado";
    private static final String EMPLEADO_PASSWORD = "emple123";
    private static final String CLIENTE_USERNAME = "cliente";
    private static final String CLIENTE_PASSWORD = "clien123";

    private final RolRepository rolRepo;
    private final PersonaRepository personaRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;

    public DataLoader(RolRepository rolRepo,
                      PersonaRepository personaRepo,
                      UsuarioRepository usuarioRepo,
                      PasswordEncoder encoder) {
        this.rolRepo = rolRepo;
        this.personaRepo = personaRepo;
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Rol rolAdmin = crearRolSiNoExiste("ADMIN", "Administrador del sistema");
        Rol rolEmpleado = crearRolSiNoExiste("EMPLEADO", "Personal de agencia");
        Rol rolCliente = crearRolSiNoExiste("CLIENTE", "Cliente o remitente");

        // Creamos los 3 usuarios
        crearAdminSiNoExiste(rolAdmin);
        crearEmpleadoSiNoExiste(rolEmpleado);
        crearClienteSiNoExiste(rolCliente);
    }

    private Rol crearRolSiNoExiste(String nombre, String descripcion) {
        return rolRepo.findByNombre(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            return rolRepo.save(rol);
        });
    }

    private void crearAdminSiNoExiste(Rol rol) {
        if (usuarioRepo.findByUsername(ADMIN_USERNAME).isPresent()) return;

        Persona persona = new Persona();
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("00000001");
        persona.setNombres("Administrador");
        persona.setActivo(true);
        persona = personaRepo.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setUsername(ADMIN_USERNAME);
        usuario.setPasswordHash(encoder.encode(ADMIN_PASSWORD));
        usuario.setActivo(true);
        usuario.getRoles().add(rol);
        usuarioRepo.save(usuario);

        System.out.println("✅ Admin creado: " + ADMIN_USERNAME + " / " + ADMIN_PASSWORD);
    }

    private void crearEmpleadoSiNoExiste(Rol rol) {
        if (usuarioRepo.findByUsername(EMPLEADO_USERNAME).isPresent()) return;

        Persona persona = new Persona();
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("00000002");
        persona.setNombres("Empleado");
        persona.setApellidos("Agencia");
        persona.setActivo(true);
        persona = personaRepo.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setUsername(EMPLEADO_USERNAME);
        usuario.setPasswordHash(encoder.encode(EMPLEADO_PASSWORD));
        usuario.setActivo(true);
        usuario.getRoles().add(rol);
        usuarioRepo.save(usuario);

        System.out.println("✅ Empleado creado: " + EMPLEADO_USERNAME + " / " + EMPLEADO_PASSWORD);
    }

    private void crearClienteSiNoExiste(Rol rol) {
        if (usuarioRepo.findByUsername(CLIENTE_USERNAME).isPresent()) return;

        Persona persona = new Persona();
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("00000003");
        persona.setNombres("Cliente");
        persona.setApellidos("Web");
        persona.setActivo(true);
        persona = personaRepo.save(persona);

        Usuario usuario = new Usuario();
        usuario.setPersona(persona);
        usuario.setUsername(CLIENTE_USERNAME);
        usuario.setPasswordHash(encoder.encode(CLIENTE_PASSWORD));
        usuario.setActivo(true);
        usuario.getRoles().add(rol);
        usuarioRepo.save(usuario);

        System.out.println("✅ Cliente creado: " + CLIENTE_USERNAME + " / " + CLIENTE_PASSWORD);
    }
}