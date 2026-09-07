package com.ronda.api.dto.request;

import com.ronda.api.enums.EstadoArticulo;

import java.math.BigDecimal;
import java.util.List;

public record PublicacionRequestDto(
        String titulo,
        String descripcion,
        Long categoriaId,
        BigDecimal precio,
        EstadoArticulo estadoArticulo,
        String zonaEntrega,
        List<String> fotosUrls
) {}
