package com.ronda.api.dto.response;

public record NovedadDto(
        Long id,
        String tipo,
        String mensaje,
        Long publicacionId,
        Long ofertaId
) { }