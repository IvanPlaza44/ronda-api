package com.ronda.api.dto.response;

import com.ronda.api.enums.EstadoArticulo;
import com.ronda.api.enums.EstadoPublicacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublicacionResumenDto(
        Long id,
        String titulo,
        BigDecimal precio,
        EstadoArticulo estadoArticulo,
        EstadoPublicacion estado,
        String zonaEntrega,
        String fotoPrincipal,
        LocalDateTime fechaPublicacion,
        String vendedorNombre
) {}
