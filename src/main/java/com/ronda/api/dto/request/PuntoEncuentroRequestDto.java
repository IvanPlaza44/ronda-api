package com.ronda.api.dto.request;

public record PuntoEncuentroRequestDto(
        String direccion,
        Double latitud,
        Double longitud
) {}
