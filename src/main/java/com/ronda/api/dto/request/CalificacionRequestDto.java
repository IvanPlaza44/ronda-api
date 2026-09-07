package com.ronda.api.dto.request;

import com.ronda.api.enums.RolCalificacion;

public record CalificacionRequestDto(
        Long publicacionId,
        RolCalificacion rolReceptor,
        Integer estrellas,
        String comentario
) {}
