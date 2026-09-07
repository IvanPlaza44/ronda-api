package com.ronda.api.dto.response;

public record FavoritoResponseDto(
        Long favoritoId,
        PublicacionResumenDto publicacion,
        boolean cambioPrecio
) {}
