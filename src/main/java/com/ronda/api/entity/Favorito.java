package com.ronda.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "publicacion_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    /** Ultimo precio visto por el usuario, para detectar cambios de precio */
    private java.math.BigDecimal ultimoPrecioVisto;

    @Builder.Default
    private LocalDateTime fechaAgregado = LocalDateTime.now();
}
