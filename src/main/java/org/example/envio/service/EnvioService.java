package org.example.envio.service;

import org.example.agencia.model.Agencia;
import org.example.agencia.repository.AgenciaRepository;
import org.example.envio.dto.PreRegistroDTO;
import org.example.envio.model.Admision;
import org.example.envio.model.DecisionAdmision;
import org.example.envio.model.Envio;
import org.example.envio.model.EstadoEnvio;
import org.example.envio.repository.EnvioRepository;
import org.example.persona.model.Persona;
import org.example.persona.model.TipoDocumento;
import org.example.persona.repository.PersonaRepository;
import org.example.shared.ValidadorDocumento; // <-- IMPORT AGREGADO
import org.example.usuario.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final PersonaRepository personaRepository;
    private final AgenciaRepository agenciaRepository;

    public EnvioService(EnvioRepository envioRepository, PersonaRepository personaRepository, AgenciaRepository agenciaRepository) {
        this.envioRepository = envioRepository;
        this.personaRepository = personaRepository;
        this.agenciaRepository = agenciaRepository;
    }

    @Transactional
    public Envio registrarPreRegistro(PreRegistroDTO dto, Usuario clienteLogueado) {

        // ✅ VALIDACIONES AGREGADAS AQUÍ
        ValidadorDocumento.validar(dto.getRemitenteTipoDocumento(), dto.getRemitenteDni());
        ValidadorDocumento.validar(dto.getDestinatarioTipoDocumento(), dto.getDestinatarioDni());

        // 1. Buscar o crear Remitente
        Persona remitente = personaRepository.findByTipoDocumentoAndNumeroDocumento(dto.getRemitenteTipoDocumento(), dto.getRemitenteDni())
                .orElseGet(() -> {
                    Persona p = new Persona();
                    p.setTipoDocumento(dto.getRemitenteTipoDocumento());
                    p.setNumeroDocumento(dto.getRemitenteDni());
                    p.setNombres(dto.getRemitenteNombres());
                    p.setApellidos(dto.getRemitenteApellidos());
                    p.setRazonSocial(dto.getRemitenteRazonSocial());
                    p.setTelefono(dto.getRemitenteCelular());
                    p.setDireccion(dto.getRemitenteDireccion());
                    return personaRepository.save(p);
                });

        // 2. Buscar o crear Destinatario
        Persona destinatario = personaRepository.findByTipoDocumentoAndNumeroDocumento(dto.getDestinatarioTipoDocumento(), dto.getDestinatarioDni())
                .orElseGet(() -> {
                    Persona p = new Persona();
                    p.setTipoDocumento(dto.getDestinatarioTipoDocumento());
                    p.setNumeroDocumento(dto.getDestinatarioDni());
                    p.setNombres(dto.getDestinatarioNombres());
                    p.setApellidos(dto.getDestinatarioApellidos());
                    p.setRazonSocial(dto.getDestinatarioRazonSocial());
                    p.setTelefono(dto.getDestinatarioCelular());
                    p.setDireccion(dto.getDestinatarioDireccion());
                    return personaRepository.save(p);
                });

        // 3. Obtener Agencias
        Agencia agenciaOrigen = agenciaRepository.findById(dto.getAgenciaOrigenId())
                .orElseThrow(() -> new RuntimeException("Agencia origen no encontrada"));
        Agencia agenciaDestino = agenciaRepository.findById(dto.getAgenciaDestinoId())
                .orElseThrow(() -> new RuntimeException("Agencia destino no encontrada"));

        // 4. Crear la Admisión
        Admision admision = new Admision();
        admision.setRemitente(remitente);
        admision.setContenidoDeclarado(dto.getDescripcionBulto());
        admision.setPesoKg(BigDecimal.valueOf(dto.getPesoEstimado()));
        admision.setLargoCm(BigDecimal.ONE);
        admision.setAnchoCm(BigDecimal.ONE);
        admision.setAltoCm(BigDecimal.ONE);
        admision.setDecision(DecisionAdmision.PENDIENTE);
        admision.setFecha(LocalDateTime.now());

        // 5. Crear el Envío
        Envio envio = new Envio();
        envio.setNumeroGuia("ENC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        envio.setAdmision(admision);
        envio.setRemitente(remitente);
        envio.setDestinatario(destinatario);
        envio.setAgenciaOrigen(agenciaOrigen);
        envio.setAgenciaDestino(agenciaDestino);
        envio.setImporteCotizado(BigDecimal.ZERO);
        envio.setEstado(EstadoEnvio.REGISTRADO);

        return envioRepository.save(envio);
    }
}