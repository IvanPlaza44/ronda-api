package com.ronda.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PerfilPublicoResponseDto(
        Long id,
        String nombre,
        String zona,
        LocalDateTime fechaAlta,
        Double promedioEstrellas,
        long totalCalificaciones,
        List<PublicacionResumenDto> publicacionesActivas
) {}
