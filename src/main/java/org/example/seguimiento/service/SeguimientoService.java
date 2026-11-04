package org.example.seguimiento.service;

import org.example.envio.model.Envio;
import org.example.envio.model.EstadoEnvio;
import org.example.envio.repository.EnvioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SeguimientoService {

    private final EnvioRepository envioRepository;

    public SeguimientoService(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    @Transactional
    public void cambiarEstado(Long envioId, EstadoEnvio nuevoEstado) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        // Validar transiciones lógicas
        if (envio.getEstado() == EstadoEnvio.EN_TRANSITO && nuevoEstado == EstadoEnvio.DISPONIBLE_RECOJO) {
            envio.setEstado(nuevoEstado);
        } else if (envio.getEstado() == EstadoEnvio.DISPONIBLE_RECOJO && nuevoEstado == EstadoEnvio.ENTREGADO) {
            envio.setEstado(nuevoEstado);
            envio.setFechaEntrega(LocalDateTime.now());
        } else {
            throw new RuntimeException("Cambio de estado no permitido.");
        }

        envioRepository.save(envio);
    }
}