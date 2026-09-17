package com.ronda.api.entity;

import com.ronda.api.enums.RolCalificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "calificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receptor_id")
    private Usuario receptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    /** Operación concreta (entrega) que se está calificando. Nula para calificaciones legacy sin operación asociada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_id")
    private Operacion operacion;

    /** Rol que tuvo el receptor en la operacion calificada */
    @Enumerated(EnumType.STRING)
    private RolCalificacion rolReceptor;

    @Column(nullable = false)
    private Integer estrellas;

    private String comentario;

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
