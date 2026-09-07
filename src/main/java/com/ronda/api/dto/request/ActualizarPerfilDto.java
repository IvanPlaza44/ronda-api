package com.ronda.api.dto.request;

public record ActualizarPerfilDto(
        String nombre,
        String telefono,
        String zona,
        String username,
        String password
) {}
