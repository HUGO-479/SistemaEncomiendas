package org.example.cliente.repository;

import org.example.cliente.model.Persona;
import org.example.cliente.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipo, String numero);
}