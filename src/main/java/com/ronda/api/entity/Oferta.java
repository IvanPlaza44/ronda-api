package com.ronda.api.entity;

import com.ronda.api.enums.EstadoOferta;
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

    @Column(length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoOferta estado = EstadoOferta.PENDIENTE;

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    private LocalDateTime fechaVencimiento;

    /** Si esta oferta es una contraoferta, referencia a la oferta que responde/reemplaza */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_origen_id")
    private Oferta ofertaOrigen;
}
