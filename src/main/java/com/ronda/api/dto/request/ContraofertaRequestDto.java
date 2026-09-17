package com.ronda.api.dto.request;

import java.math.BigDecimal;

public record ContraofertaRequestDto(
        BigDecimal monto,
        String mensaje
) {}
