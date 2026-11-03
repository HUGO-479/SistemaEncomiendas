package org.example.shared;

import org.example.persona.model.TipoDocumento;

public class ValidadorDocumento {

    public static void validar(TipoDocumento tipo, String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new RuntimeException("El número de documento no puede estar vacío.");
        }

        switch (tipo) {
            case DNI:
                if (!numero.matches("^[0-9]{8}$")) {
                    throw new RuntimeException("El DNI debe tener exactamente 8 dígitos numéricos.");
                }
                break;
            case RUC:
                if (!numero.matches("^[0-9]{11}$")) {
                    throw new RuntimeException("El RUC debe tener exactamente 11 dígitos numéricos.");
                }
                break;
            case PASAPORTE:
                if (!numero.matches("^[A-Za-z0-9]{9}$")) {
                    throw new RuntimeException("El Pasaporte debe tener exactamente 9 caracteres alfanuméricos.");
                }
                break;
            default:
                throw new RuntimeException("Tipo de documento no válido.");
        }
    }
}
