package com.ronda.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "busquedas_guardadas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusquedaGuardada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String nombre;

    private String query;
    private Long categoriaId;
    private java.math.BigDecimal precioMin;
    private java.math.BigDecimal precioMax;

    @Enumerated(EnumType.STRING)
    private com.ronda.api.enums.EstadoArticulo estadoArticulo;

    private String zona;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Marca de la ultima vez que el usuario revisó esta búsqueda (para calcular novedades) */
    @Builder.Default
    private LocalDateTime ultimaRevision = LocalDateTime.now();
}
