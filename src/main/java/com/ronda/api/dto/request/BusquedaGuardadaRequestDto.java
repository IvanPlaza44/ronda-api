package com.ronda.api.dto.request;

import com.ronda.api.enums.EstadoArticulo;

import java.math.BigDecimal;

public record BusquedaGuardadaRequestDto(
        String nombre,
        String query,
        Long categoriaId,
        BigDecimal precioMin,
        BigDecimal precioMax,
        EstadoArticulo estadoArticulo,
        String zona
) {}
