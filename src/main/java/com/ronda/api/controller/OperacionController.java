package com.ronda.api.controller;

import com.ronda.api.dto.request.CalificacionOperacionRequestDto;
import com.ronda.api.dto.request.PuntoEncuentroRequestDto;
import com.ronda.api.dto.response.OperacionResponseDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.service.CalificacionService;
import com.ronda.api.service.OperacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operaciones")
@RequiredArgsConstructor
public class OperacionController {

    private final OperacionService operacionService;
    private final CalificacionService calificacionService;

    @GetMapping("/mias")
    public ResponseEntity<List<OperacionResponseDto>> listarMias(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(operacionService.listarMias(usuario));
    }

    @GetMapping("/{operacionId}")
    public ResponseEntity<OperacionResponseDto> obtenerDetalle(@AuthenticationPrincipal Usuario usuario,
                                                                  @PathVariable Long operacionId) {
        return ResponseEntity.ok(operacionService.obtenerDetalle(usuario, operacionId));
    }

    @PutMapping("/{operacionId}/punto-encuentro")
    public ResponseEntity<OperacionResponseDto> actualizarPuntoEncuentro(@AuthenticationPrincipal Usuario usuario,
                                                                            @PathVariable Long operacionId,
                                                                            @RequestBody PuntoEncuentroRequestDto dto) {
        return ResponseEntity.ok(operacionService.actualizarPuntoEncuentro(usuario, operacionId, dto));
    }

    @PutMapping("/{operacionId}/entregada")
    public ResponseEntity<OperacionResponseDto> marcarEntregada(@AuthenticationPrincipal Usuario usuario,
                                                                   @PathVariable Long operacionId) {
        return ResponseEntity.ok(operacionService.marcarEntregada(usuario, operacionId));
    }

    @PostMapping("/{operacionId}/calificar")
    public ResponseEntity<Void> calificar(@AuthenticationPrincipal Usuario usuario,
                                            @PathVariable Long operacionId,
                                            @RequestBody CalificacionOperacionRequestDto dto) {
        calificacionService.calificarPorOperacion(usuario, operacionId, dto);
        return ResponseEntity.status(201).build();
    }
}
