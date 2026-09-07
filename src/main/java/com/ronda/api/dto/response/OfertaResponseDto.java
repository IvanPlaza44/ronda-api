package com.ronda.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OfertaResponseDto(
        Long id,
        String autorNombre,
        BigDecimal monto,
        String estado,
        LocalDateTime fecha
) {}
