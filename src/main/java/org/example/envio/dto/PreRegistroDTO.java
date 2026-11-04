package org.example.envio.dto;

import lombok.Data;
import org.example.persona.model.TipoDocumento;

import java.math.BigDecimal;

@Data
public class PreRegistroDTO {
    // --- REMITENTE ---
    private TipoDocumento remitenteTipoDocumento;
    private String remitenteDni;
    private String remitenteNombres;
    private String remitenteApellidos;
    private String remitenteRazonSocial;
    private String remitenteCelular;
    private String remitenteDireccion;

    // --- DESTINATARIO ---
    private TipoDocumento destinatarioTipoDocumento;
    private String destinatarioDni;
    private String destinatarioNombres;
    private String destinatarioApellidos;
    private String destinatarioRazonSocial;
    private String destinatarioCelular;
    private String destinatarioDireccion;

    // --- ENVÍO ---

    private Long agenciaOrigenId;   // <--- AGREGAR
    private Long agenciaDestinoId;  // <--- AGREGAR
    private Long tipoPaqueteId;     // <--- AGREGAR
    private String descripcionBulto;
    private Double pesoEstimado;

    private BigDecimal largoCm;
    private BigDecimal anchoCm;
    private BigDecimal altoCm;





}