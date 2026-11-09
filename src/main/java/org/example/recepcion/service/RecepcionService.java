package org.example.recepcion.service;

import org.example.agencia.model.Agencia;
import org.example.agencia.repository.AgenciaRepository;
import org.example.envio.model.Admision;
import org.example.envio.model.DecisionAdmision;
import org.example.envio.model.Envio;
import org.example.envio.model.EstadoEnvio;
import org.example.envio.repository.EnvioRepository;
import org.example.cliente.model.Persona;
import org.example.cliente.model.TipoDocumento;
import org.example.cliente.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RecepcionService {

    private final EnvioRepository envioRepository;
    private final ClienteRepository clienteRepository;
    private final AgenciaRepository agenciaRepository;

    public RecepcionService(EnvioRepository envioRepository,
                            ClienteRepository clienteRepository,
                            AgenciaRepository agenciaRepository) {
        this.envioRepository = envioRepository;
        this.clienteRepository = clienteRepository;
        this.agenciaRepository = agenciaRepository;
    }

    // ==========================================
    // FLUJO 1: BUSCAR PRE-REGISTRO WEB POR DNI
    // ==========================================
    public List<Envio> buscarPendientes(String busqueda) {
        return envioRepository.buscarPorGuiaODocumento(busqueda)
                .stream()
                .filter(e -> e.getEstado() == EstadoEnvio.REGISTRADO)
                .toList();
    }

    // ==========================================
    // FLUJO 2: REGISTRO DIRECTO EN AGENCIA
    // ==========================================
    @Transactional
    public Envio registrarDirectoEnAgencia(
            TipoDocumento remitenteTipoDoc, String remitenteDni,
            String remitenteNombres, String remitenteApellidos, String remitenteRazonSocial, String remitenteCelular,
            TipoDocumento destinatarioTipoDoc, String destinatarioDni,
            String destinatarioNombres, String destinatarioApellidos, String destinatarioRazonSocial, String destinatarioCelular,
            Long agenciaDestinoId, String descripcionBulto, Agencia agenciaOrigen) {

        // 1. Buscar o crear Remitente
        Persona remitente = clienteRepository.findByTipoDocumentoAndNumeroDocumento(remitenteTipoDoc, remitenteDni)
                .orElseGet(() -> {
                    Persona p = new Persona();
                    p.setTipoDocumento(remitenteTipoDoc);
                    p.setNumeroDocumento(remitenteDni);
                    p.setNombres(remitenteNombres);
                    p.setApellidos(remitenteApellidos);
                    p.setRazonSocial(remitenteRazonSocial);
                    p.setTelefono(remitenteCelular);
                    return clienteRepository.save(p);
                });

        // 2. Buscar o crear Destinatario
        Persona destinatario = clienteRepository.findByTipoDocumentoAndNumeroDocumento(destinatarioTipoDoc, destinatarioDni)
                .orElseGet(() -> {
                    Persona p = new Persona();
                    p.setTipoDocumento(destinatarioTipoDoc);
                    p.setNumeroDocumento(destinatarioDni);
                    p.setNombres(destinatarioNombres);
                    p.setApellidos(destinatarioApellidos);
                    p.setRazonSocial(destinatarioRazonSocial);
                    p.setTelefono(destinatarioCelular);
                    return clienteRepository.save(p);
                });

        // 3. Obtener Agencia Destino
        Agencia agenciaDestino = agenciaRepository.findById(agenciaDestinoId)
                .orElseThrow(() -> new RuntimeException("Agencia destino no encontrada"));

        // 4. Crear Admisión (Medidas provisionales)
        Admision admision = new Admision();
        admision.setRemitente(remitente);
        admision.setContenidoDeclarado(descripcionBulto);
        admision.setPesoKg(BigDecimal.ONE);
        admision.setLargoCm(BigDecimal.ONE);
        admision.setAnchoCm(BigDecimal.ONE);
        admision.setAltoCm(BigDecimal.ONE);
        admision.setDecision(DecisionAdmision.PENDIENTE);
        admision.setFecha(LocalDateTime.now());

        // 5. Crear Envío
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

    // ==========================================
    // FLUJO 3: CONFIRMAR RECEPCIÓN (PESAJE)
    // ==========================================
    @Transactional
    public void confirmarRecepcion(Long envioId, Double pesoReal, Double largo, Double ancho, Double alto) {
        // 1. Buscamos el envío
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        // 2. Actualizamos la Admisión con las medidas reales
        Admision admision = envio.getAdmision();
        admision.setPesoKg(BigDecimal.valueOf(pesoReal));
        admision.setLargoCm(BigDecimal.valueOf(largo));
        admision.setAnchoCm(BigDecimal.valueOf(ancho));
        admision.setAltoCm(BigDecimal.valueOf(alto));
        admision.setDecision(DecisionAdmision.ACEPTADA);

        // 3. Cambiamos el estado del envío
        envio.setEstado(EstadoEnvio.EN_TRANSITO);

        // 4. Guardamos
        envioRepository.save(envio);
    }
}