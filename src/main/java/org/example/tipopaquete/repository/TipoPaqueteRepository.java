package org.example.tipopaquete.repository;

import org.example.tipopaquete.model.TipoPaquete;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoPaqueteRepository
        extends JpaRepository<TipoPaquete, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );
}