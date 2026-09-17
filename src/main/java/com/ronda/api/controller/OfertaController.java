package com.ronda.api.controller;

import com.ronda.api.dto.request.ContraofertaRequestDto;
import com.ronda.api.dto.response.OfertaResponseDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.service.PreguntaOfertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Acciones sobre una oferta puntual (aceptar, rechazar, contraofertar) y listados propios.
 * La creación/listado de ofertas de una publicación vive en PreguntaOfertaController
 * bajo /api/publicaciones/{publicacionId}/ofertas.
 */
@RestController
@RequestMapping("/api/ofertas")
@RequiredArgsConstructor
public class OfertaController {

    private final PreguntaOfertaService preguntaOfertaService;

    @PutMapping("/{ofertaId}/aceptar")
    public ResponseEntity<OfertaResponseDto> aceptar(@AuthenticationPrincipal Usuario usuario,
                                                        @PathVariable Long ofertaId) {
        return ResponseEntity.ok(preguntaOfertaService.aceptarOferta(usuario, ofertaId));
    }

    @PutMapping("/{ofertaId}/rechazar")
    public ResponseEntity<OfertaResponseDto> rechazar(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable Long ofertaId) {
        return ResponseEntity.ok(preguntaOfertaService.rechazarOferta(usuario, ofertaId));
    }

    @PostMapping("/{ofertaId}/contraofertar")
    public ResponseEntity<OfertaResponseDto> contraofertar(@AuthenticationPrincipal Usuario usuario,
                                                              @PathVariable Long ofertaId,
                                                              @RequestBody ContraofertaRequestDto dto) {
        return ResponseEntity.status(201).body(preguntaOfertaService.contraofertar(usuario, ofertaId, dto));
    }

    @GetMapping("/enviadas")
    public ResponseEntity<List<OfertaResponseDto>> misOfertasEnviadas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(preguntaOfertaService.misOfertasEnviadas(usuario));
    }

    @GetMapping("/recibidas")
    public ResponseEntity<List<OfertaResponseDto>> misOfertasRecibidas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(preguntaOfertaService.misOfertasRecibidas(usuario));
    }
}
