package com.ronda.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OfertaResponseDto(
        Long id,
        Long publicacionId,
        String publicacionTitulo,
        Long autorId,
        String autorNombre,
        BigDecimal monto,
        String mensaje,
        String estado,
        LocalDateTime fecha,
        LocalDateTime fechaVencimiento,
        String tipo // OFERTA o CONTRAOFERTA
) {}
