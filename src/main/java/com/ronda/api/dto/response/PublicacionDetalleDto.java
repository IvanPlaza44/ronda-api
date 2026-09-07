package com.ronda.api.dto.response;

import com.ronda.api.enums.EstadoArticulo;
import com.ronda.api.enums.EstadoPublicacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PublicacionDetalleDto(
        Long id,
        String titulo,
        String descripcion,
        String categoria,
        BigDecimal precio,
        EstadoArticulo estadoArticulo,
        EstadoPublicacion estado,
        String zonaEntrega,
        LocalDateTime fechaPublicacion,
        List<String> fotos,
        Long vendedorId,
        String vendedorNombre,
        Double vendedorPromedioEstrellas,
        boolean esPropia
) {}
