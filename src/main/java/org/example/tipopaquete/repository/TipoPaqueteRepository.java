package org.example.tipopaquete.repository;

import org.example.tipopaquete.model.TipoPaquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; // <-- Importante para el Optional

public interface TipoPaqueteRepository extends JpaRepository<TipoPaquete, Long> {

    // Métodos que ya tenías
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    //MÉTODO QUE FALTABA (para que el Service no marque error)
    Optional<TipoPaquete> findByNombreIgnoreCase(String nombre);
}