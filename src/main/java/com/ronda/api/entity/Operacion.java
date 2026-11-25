package com.ronda.api.entity;

import com.ronda.api.enums.EstadoOperacion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id")
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @Column(nullable = false)
    private BigDecimal montoFinal;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoOperacion estado = EstadoOperacion.PENDIENTE_ENTREGA;

    @Builder.Default
    private LocalDateTime fechaAcordada = LocalDateTime.now();

    private LocalDateTime fechaEntrega;

    // Coordinación de entrega / punto de encuentro (para el mapa)
    private String direccionEncuentro;
    private Double latitudEncuentro;
    private Double longitudEncuentro;
}
