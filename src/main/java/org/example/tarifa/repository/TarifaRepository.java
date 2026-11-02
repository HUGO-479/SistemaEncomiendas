
package org.example.tarifa.repository;

import org.example.tarifa.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifaRepository
        extends JpaRepository<Tarifa, Long> {

    boolean existsByAgenciaOrigenIdAndAgenciaDestinoId(
            Long origenId, Long destinoId);

    boolean existsByAgenciaOrigenIdAndAgenciaDestinoIdAndIdNot(
            Long origenId, Long destinoId, Long id);
}
