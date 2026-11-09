package org.example.envio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.cliente.model.Persona;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "admisiones")
public class Admision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "remitente_id", nullable = false)
    private Persona remitente;

    @Column(name = "contenido_declarado", nullable = false, length = 500)
    private String contenidoDeclarado;

    @Column(name = "peso_kg", nullable = false)
    private BigDecimal pesoKg;

    @Column(name = "largo_cm", nullable = false)
    private BigDecimal largoCm;

    @Column(name = "ancho_cm", nullable = false)
    private BigDecimal anchoCm;

    @Column(name = "alto_cm", nullable = false)
    private BigDecimal altoCm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionAdmision decision = DecisionAdmision.PENDIENTE;

    private LocalDateTime fecha;

}