package com.ronda.api.dto.response;

public record PerfilResponseDto(
        Long id,
        String nombre,
        String email,
        String username,
        String telefono,
        String zona,
        String fotoPerfil,
        java.time.LocalDateTime fechaAlta,
        Double promedioEstrellas,
        long operacionesComoComprador,
        long operacionesComoVendedor
) {}
