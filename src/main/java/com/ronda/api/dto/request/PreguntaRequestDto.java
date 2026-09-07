package com.ronda.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PreguntaRequestDto(
        @NotBlank String mensaje
) {}
