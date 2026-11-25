package com.ronda.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;          // quién recibe el aviso

    @Column(nullable = false)
    private String tipo;             // PREGUNTA_NUEVA, OFERTA_ACEPTADA, etc.

    @Column(nullable = false, length = 500)
    private String mensaje;

    private Long publicacionId;
    private Long ofertaId;

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Builder.Default
    private boolean entregada = false;   // true cuando ya se le mandó a la app
}