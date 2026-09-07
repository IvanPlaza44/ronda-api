package com.ronda.api.dto.response;

public record BusquedaGuardadaResponseDto(
        Long id,
        String nombre,
        boolean hayNovedades
) {}
