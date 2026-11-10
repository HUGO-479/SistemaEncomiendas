package org.example.agencia.repository;

import org.example.agencia.model.Agencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Agrega esta importación

public interface AgenciaRepository extends JpaRepository<Agencia, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    // AGREGA ESTE MÉTODO:
    List<Agencia> findByIdNot(Long id);
}