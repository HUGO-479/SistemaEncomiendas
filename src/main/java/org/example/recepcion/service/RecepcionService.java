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
import java.math.RoundingMode;
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
    // FLUJO 1: BUSCAR PRE-REGISTRO WEB
    // ==========================================
    // MODIFICADO: Ahora recibe la agencia del empleado para filtrar
    public List<Envio> buscarPendientes(String busqueda, Agencia agenciaEmpleado) {
        return envioRepository.buscarPorGuiaODocumento(busqueda)
                .stream()
                .filter(e -> e.getEstado() == EstadoEnvio.REGISTRADO)
                // FILTRO: Excluimos los envíos cuyo destino sea la misma agencia del empleado
                .filter(e -> !e.getAgenciaDestino().getId().equals(agenciaEmpleado.getId()))
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
            Long agenciaDestinoId, Long tipoPaqueteId, String descripcionBulto, Agencia agenciaOrigen) {

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

        // VALIDACIÓN DE SEGURIDAD
        if (agenciaOrigen.getId().equals(agenciaDestino.getId())) {
            throw new RuntimeException("Error: No puedes registrar un envío a la misma agencia donde te encuentras.");
        }

        // 4. Crear Admisión (Valores provisionales, se corrigen al pesar)
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
        envio.setImporteCotizado(BigDecimal.ZERO); // Se calcula al pesar
        envio.setEstado(EstadoEnvio.REGISTRADO);

        return envioRepository.save(envio);
    }

    // ==========================================
    // FLUJO 3: CONFIRMAR RECEPCIÓN Y CALCULAR PRECIO
    // ==========================================
    @Transactional
    public void confirmarRecepcion(Long envioId, Double pesoReal, Double largo, Double ancho, Double alto) {

        if (pesoReal == null || pesoReal <= 0 || largo == null || largo <= 0 || ancho == null || ancho <= 0 || alto == null || alto <= 0) {
            throw new RuntimeException("El peso y las medidas deben ser mayores a cero.");
        }

        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        BigDecimal precioCalculado = calcularPrecio(pesoReal, largo, ancho, alto);

        Admision admision = envio.getAdmision();
        admision.setPesoKg(BigDecimal.valueOf(pesoReal));
        admision.setLargoCm(BigDecimal.valueOf(largo));
        admision.setAnchoCm(BigDecimal.valueOf(ancho));
        admision.setAltoCm(BigDecimal.valueOf(alto));
        admision.setDecision(DecisionAdmision.ACEPTADA);

        envio.setImporteCotizado(precioCalculado);
        envio.setEstado(EstadoEnvio.EN_AGENCIA);

        envioRepository.save(envio);
    }

    // ==========================================
    // LÓGICA DE CÁLCULO
    // ==========================================
    // Reemplaza tu método calcularPrecio actual por este:
    private BigDecimal calcularPrecio(Double pesoReal, Double largo, Double ancho, Double alto) {
        // 1. Calcular peso volumétrico
        double volumen = largo * ancho * alto;
        double pesoVolumetrico = volumen / 5000.0;

        // 2. Obtener el Peso Facturable (El mayor)
        double pesoFacturable = Math.max(pesoReal, pesoVolumetrico);

        // 3. Redondear HACIA ARRIBA al kilo completo (Ej: 2.1 -> 3)
        BigDecimal kilosFacturables = BigDecimal.valueOf(pesoFacturable).setScale(0, RoundingMode.CEILING);

        // 4. Validar el límite de 20 kg
        if (kilosFacturables.compareTo(new BigDecimal("20")) > 0) {
            throw new RuntimeException("El paquete supera los 20 kg. Debe cotizarse como carga especial en ventanilla.");
        }

        // 5. Aplicar la fórmula: Base 12.00 + (Kilos extra * 1.50)
        BigDecimal tarifaBase = new BigDecimal("12.00");

        if (kilosFacturables.compareTo(new BigDecimal("2")) <= 0) {
            return tarifaBase; // 1 o 2 kg
        } else {
            BigDecimal kilosExtra = kilosFacturables.subtract(new BigDecimal("2"));
            BigDecimal costoExtra = kilosExtra.multiply(new BigDecimal("1.50"));
            return tarifaBase.add(costoExtra);
        }
    }

    // Método público para que puedas usarlo en el controlador o en la vista si lo necesitas
    public String obtenerCategoria(BigDecimal pesoFacturableBD) {
        int peso = pesoFacturableBD.intValue();
        if (peso <= 2) return "Pequeño";
        if (peso <= 5) return "Mediano";
        if (peso <= 10) return "Grande";
        return "Industrial";
        }
    }
