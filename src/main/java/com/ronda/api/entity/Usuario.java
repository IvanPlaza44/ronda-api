package com.ronda.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    /** Null si el usuario solo se registro/loguea via OTP y nunca definio contraseña */
    private String password;

    private String nombre;

    private String telefono;

    private String zona;

    @Builder.Default
    private boolean emailVerificado = false;

    @Builder.Default
    private LocalDateTime fechaAlta = LocalDateTime.now();

    @OneToMany(mappedBy = "vendedor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Publicacion> publicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "receptor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Calificacion> calificacionesRecibidas = new ArrayList<>();
}
