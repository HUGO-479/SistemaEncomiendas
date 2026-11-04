package org.example.envio.repository;

import org.example.envio.model.Envio;
import org.example.persona.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByNumeroGuia(String numeroGuia);
    List<Envio> findByRemitenteOrDestinatario(Persona remitente, Persona destinatario);
    @Query("SELECT e FROM Envio e WHERE e.numeroGuia = :busqueda OR e.remitente.numeroDocumento = :busqueda OR e.destinatario.numeroDocumento = :busqueda")
    List<Envio> buscarPorGuiaODocumento(@Param("busqueda") String busqueda);

}
