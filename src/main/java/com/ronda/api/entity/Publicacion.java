package com.ronda.api.entity;

import com.ronda.api.enums.EstadoArticulo;
import com.ronda.api.enums.EstadoPublicacion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    private String titulo;

    @Column(length = 2000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    private EstadoArticulo estadoArticulo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPublicacion estado = EstadoPublicacion.BORRADOR;

    private String zonaEntrega;

    @Builder.Default
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    @Builder.Default
    private List<Foto> fotos = new ArrayList<>();

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pregunta> preguntas = new ArrayList<>();

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Oferta> ofertas = new ArrayList<>();
}
