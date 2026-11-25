package com.ronda.api.dto.response;

import com.ronda.api.enums.EstadoArticulo;

import java.math.BigDecimal;

public record BusquedaGuardadaResponseDto(
        Long id,
        String nombre,
        boolean hayNovedades,
        String query,
        Long categoriaId,
        BigDecimal precioMin,
        BigDecimal precioMax,
        EstadoArticulo estadoArticulo,
        String zona
) {}
