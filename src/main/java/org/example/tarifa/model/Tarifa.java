
package org.example.tarifa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.agencia.model.Agencia;

import java.math.BigDecimal;

@Entity
@Table(name = "tarifas", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_tarifa_ruta",
        columnNames = {"agencia_origen_id", "agencia_destino_id"}
    )
})
@Getter
@Setter
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "agencia_origen_id", nullable = false)
    private Agencia agenciaOrigen;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "agencia_destino_id", nullable = false)
    private Agencia agenciaDestino;

    @Column(name = "precio_base", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "precio_por_kg", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal precioPorKg;

    @Column(nullable = false)
    private Boolean activa = true;
}
