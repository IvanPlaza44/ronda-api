package com.ronda.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperacionResponseDto(
        Long id,
        Long publicacionId,
        String publicacionTitulo,
        Long compradorId,
        String compradorNombre,
        Long vendedorId,
        String vendedorNombre,
        BigDecimal montoFinal,
        String estado,
        LocalDateTime fechaAcordada,
        LocalDateTime fechaEntrega,
        String direccionEncuentro,
        Double latitudEncuentro,
        Double longitudEncuentro,
        boolean puedeCalificar,
        String tipo // COMPRA o VENTA, relativo a quien consulta
) {}
