package com.ronda.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ofertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false)
    private BigDecimal monto;

    @Builder.Default
    private String estado = "PENDIENTE"; // PENDIENTE, ACEPTADA, RECHAZADA

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
