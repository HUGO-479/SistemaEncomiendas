package org.example.envio.service;

import org.example.agencia.model.Agencia;
import org.example.agencia.repository.AgenciaRepository;
import org.example.cliente.repository.ClienteRepository;
import org.example.envio.dto.PreRegistroDTO;
import org.example.envio.model.Admision;
import org.example.envio.model.DecisionAdmision;
import org.example.envio.model.Envio;
import org.example.envio.model.EstadoEnvio;
import org.example.envio.repository.EnvioRepository;
import org.example.cliente.model.Persona;
import org.example.cliente.model.TipoDocumento;
import org.example.shared.ValidadorDocumento;
import org.example.usuario.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final ClienteRepository personaRepository;
    private final AgenciaRepository agenciaRepository;

    public EnvioService(EnvioRepository envioRepository,
                        ClienteRepository personaRepository,
                        AgenciaRepository agenciaRepository) {
        this.envioRepository = envioRepository;
        this.personaRepository = personaRepository;
        this.agenciaRepository = agenciaRepository;
    }

    @Transactional
    public Envio registrarPreRegistro(PreRegistroDTO dto, Usuario clienteLogueado) {

        // 1. Validar Documentos
        // Para el remitente, usamos los datos de la persona del cliente logueado
        Persona personaRemitente = clienteLogueado.getPersona();
        String nombreRemitente = personaRemitente.getTipoDocumento() == TipoDocumento.RUC
                ? personaRemitente.getRazonSocial() : personaRemitente.getNombres();
        ValidadorDocumento.validar(personaRemitente.getTipoDocumento(), personaRemitente.getNumeroDocumento(), nombreRemitente);

        // Para el destinatario, usamos los datos del formulario
        String nombreDestinatario = dto.getDestinatarioTipoDocumento() == TipoDocumento.RUC
                ? dto.getDestinatarioRazonSocial() : dto.getDestinatarioNombres();
        ValidadorDocumento.validar(dto.getDestinatarioTipoDocumento(), dto.getDestinatarioDni(), nombreDestinatario);

        // 2. Asignar Remitente (El cliente logueado)
        Persona remitente = personaRemitente;

        // 3. Buscar o crear Destinatario
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
                    p.setActivo(true);
                    return personaRepository.save(p);
                });

        // 4. Obtener Agencias
        Agencia agenciaOrigen = agenciaRepository.findById(dto.getAgenciaOrigenId())
                .orElseThrow(() -> new RuntimeException("Agencia origen no encontrada"));
        Agencia agenciaDestino = agenciaRepository.findById(dto.getAgenciaDestinoId())
                .orElseThrow(() -> new RuntimeException("Agencia destino no encontrada"));

        // 5. Crear la Admisión (Medidas estimadas por defecto)
        Admision admision = new Admision();
        admision.setRemitente(remitente);
        admision.setContenidoDeclarado(dto.getDescripcionBulto());
        admision.setPesoKg(BigDecimal.valueOf(dto.getPesoEstimado()));

        admision.setLargoCm(BigDecimal.ONE);
        admision.setAnchoCm(BigDecimal.ONE);
        admision.setAltoCm(BigDecimal.ONE);

        admision.setDecision(DecisionAdmision.PENDIENTE);
        admision.setFecha(LocalDateTime.now());

        // 6. Crear el Envío
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