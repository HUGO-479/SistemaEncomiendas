package org.example.envio.dto;

import lombok.Data;
import org.example.persona.model.TipoDocumento;

@Data
public class PreRegistroDTO {
    // --- REMITENTE ---
    private TipoDocumento remitenteTipoDocumento;
    private String remitenteDni;
    private String remitenteNombres;
    private String remitenteApellidos;
    private String remitenteRazonSocial;
    private String remitenteCelular;
    private String remitenteEmail;
    private String remitenteDireccion;

    // --- DESTINATARIO ---
    private TipoDocumento destinatarioTipoDocumento;
    private String destinatarioDni;
    private String destinatarioNombres;
    private String destinatarioApellidos;
    private String destinatarioRazonSocial;
    private String destinatarioCelular;
    private String destinatarioEmail;
    private String destinatarioDireccion;

    // --- ENVÍO ---

    private Long agenciaOrigenId;   // <--- AGREGAR
    private Long agenciaDestinoId;  // <--- AGREGAR
    private Long tipoPaqueteId;     // <--- AGREGAR
    private String descripcionBulto;
    private Double pesoEstimado;

}