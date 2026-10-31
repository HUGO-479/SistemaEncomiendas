package org.example.recepcion.service;

import org.example.envio.model.Admision;
import org.example.envio.model.DecisionAdmision;
import org.example.envio.model.Envio;
import org.example.envio.model.EstadoEnvio;
import org.example.envio.repository.EnvioRepository;
import org.example.persona.model.Persona;
import org.example.persona.model.TipoDocumento;
import org.example.persona.repository.PersonaRepository;
import org.example.usuario.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RecepcionService {

    private final EnvioRepository envioRepository;
    private final PersonaRepository personaRepository;

    public RecepcionService(EnvioRepository envioRepository, PersonaRepository personaRepository) {
        this.envioRepository = envioRepository;
        this.personaRepository = personaRepository;
    }

    // ==========================================
    // CAMINO 1: Buscar pre-registro web por DNI
    // ==========================================
    public List<Envio> buscarPendientesPorDni(String dni) {
        Persona persona = personaRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, dni)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada en la base de datos."));

        return envioRepository.findByRemitenteOrDestinatario(persona, persona)
                .stream()
                .filter(e -> e.getEstado() == EstadoEnvio.REGISTRADO)
                .toList();
    }

    // ==========================================
    // CAMINO 1: Confirmar recepción (pesar y cambiar estado)
    // ==========================================
    @Transactional
    public void confirmarRecepcion(Long envioId, Double pesoFinal, Usuario empleado) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        envio.setEstado(EstadoEnvio.EN_TRANSITO);

        Admision admision = envio.getAdmision();
        if (admision != null) {
            admision.setPesoKg(BigDecimal.valueOf(pesoFinal));
            admision.setDecision(DecisionAdmision.ACEPTADA);
        }

        envioRepository.save(envio);
    }

    // ==========================================
    // CAMINO 2: REGISTRO DIRECTO EN AGENCIA
    // ==========================================
    @Transactional
    public Envio registrarDirectoEnAgencia(
            String remitenteDni, String remitenteNombres, String remitenteApellidos, String remitenteTelefono,
            String destinatarioDni, String destinatarioNombres, String destinatarioApellidos, String destinatarioTelefono,
            String contenido, Double peso, Usuario empleado) {

        // 1. Buscar o crear al Remitente
        Persona remitente = personaRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, remitenteDni)
                .orElseGet(() -> {
                    Persona p = new Persona();
                    p.setTipoDocumento(TipoDocumento.DNI);
                    p.setNumeroDocumento(remitenteDni);
                    p.setNombres(remitenteNombres);
                    p.setApellidos(remitenteApellidos);
                    p.setTelefono(remitenteTelefono);
                    return personaRepository.save(p);
                });

        // 2. Buscar o crear al Destinatario
        Persona destinatario = personaRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.DNI, destinatarioDni)
                .orElseGet(() -> {
                    Persona p = new Persona();
                    p.setTipoDocumento(TipoDocumento.DNI);
                    p.setNumeroDocumento(destinatarioDni);
                    p.setNombres(destinatarioNombres);
                    p.setApellidos(destinatarioApellidos);
                    p.setTelefono(destinatarioTelefono);
                    return personaRepository.save(p);
                });

        // 3. Crear la Admisión
        Admision admision = new Admision();
        admision.setRemitente(remitente);
        admision.setContenidoDeclarado(contenido);
        admision.setPesoKg(BigDecimal.valueOf(peso));
        admision.setLargoCm(BigDecimal.ONE); // TODO: Pedir medidas reales
        admision.setAnchoCm(BigDecimal.ONE);
        admision.setAltoCm(BigDecimal.ONE);
        admision.setDecision(DecisionAdmision.ACEPTADA);
        admision.setFecha(LocalDateTime.now());

        // 4. Crear el Envío (Canal AGENCIA)
        Envio envio = new Envio();
        envio.setNumeroGuia("ENC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        envio.setAdmision(admision);
        envio.setRemitente(remitente);
        envio.setDestinatario(destinatario);
        envio.setImporteCotizado(BigDecimal.ZERO); // TODO: Calcular con tarifa
        envio.setEstado(EstadoEnvio.EN_AGENCIA); // Ya está en agencia
        // envio.setUsuarioRegistro(empleado); // TODO: Asignar el empleado

        return envioRepository.save(envio);
    }
}