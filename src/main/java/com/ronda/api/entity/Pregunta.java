package com.ronda.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "preguntas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(length = 1000, nullable = false)
    private String mensaje;

    @Column(length = 1000)
    private String respuesta;

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
