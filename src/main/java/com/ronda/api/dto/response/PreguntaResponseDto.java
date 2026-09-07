package com.ronda.api.dto.response;

import java.time.LocalDateTime;

public record PreguntaResponseDto(
        Long id,
        String autorNombre,
        String mensaje,
        String respuesta,
        LocalDateTime fecha
) {}
