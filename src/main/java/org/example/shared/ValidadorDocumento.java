package org.example.shared;

import org.example.cliente.model.TipoDocumento;

public class ValidadorDocumento {

    // Ahora recibe 3 parámetros: tipo, número y el nombre/razón social
    public static void validar(TipoDocumento tipo, String numero, String nombreORazonSocial) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new RuntimeException("El número de documento no puede estar vacío.");
        }

        switch (tipo) {
            case DNI:
                if (!numero.matches("^[0-9]{8}$")) {
                    throw new RuntimeException("El DNI debe tener exactamente 8 dígitos numéricos.");
                }
                if (nombreORazonSocial == null || nombreORazonSocial.trim().isEmpty()) {
                    throw new RuntimeException("Los nombres son obligatorios para DNI.");
                }
                break;
            case RUC:
                if (!numero.matches("^[0-9]{11}$")) {
                    throw new RuntimeException("El RUC debe tener exactamente 11 dígitos numéricos.");
                }
                if (nombreORazonSocial == null || nombreORazonSocial.trim().isEmpty()) {
                    throw new RuntimeException("La razón social es obligatoria para RUC.");
                }
                break;
            case PASAPORTE:
                if (!numero.matches("^[A-Za-z0-9]{9}$")) {
                    throw new RuntimeException("El Pasaporte debe tener exactamente 9 caracteres alfanuméricos.");
                }
                if (nombreORazonSocial == null || nombreORazonSocial.trim().isEmpty()) {
                    throw new RuntimeException("Los nombres son obligatorios para Pasaporte.");
                }
                break;
        }
    }
}