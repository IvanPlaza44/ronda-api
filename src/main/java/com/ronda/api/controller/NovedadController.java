package com.ronda.api.controller;

import com.ronda.api.dto.response.NovedadDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/novedades")
@RequiredArgsConstructor
public class NovedadController {

    private static final long ESPERA_MAXIMA_MS = 25_000;   // cuánto retiene el pedido
    private static final long INTERVALO_MS = 1_000;        // cada cuánto revisa

    private final NotificacionService notificacionService;

    @GetMapping("/esperar")
    public List<NovedadDto> esperar(@AuthenticationPrincipal Usuario usuario)
            throws InterruptedException {

        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;

        while (System.currentTimeMillis() < limite) {
            List<NovedadDto> nuevas = notificacionService.tomarPendientes(usuario.getId());
            if (!nuevas.isEmpty()) {
                return nuevas;          // hay avisos: se devuelven enseguida
            }
            Thread.sleep(INTERVALO_MS); // no hay: esperar 1 segundo y volver a mirar
        }
        return List.of();               // pasó el tiempo sin novedades: lista vacía
    }
}