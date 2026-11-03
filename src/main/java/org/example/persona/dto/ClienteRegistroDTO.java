package org.example.persona.dto;

import lombok.Data;
import org.example.persona.model.TipoDocumento;

@Data // <--- Si usas Lombok, esto genera los getters y setters
public class ClienteRegistroDTO {
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String razonSocial; // <-- Este faltaba
    private String celular;
    private String email;       // <-- Este también puede faltar
    private String direccion;
}