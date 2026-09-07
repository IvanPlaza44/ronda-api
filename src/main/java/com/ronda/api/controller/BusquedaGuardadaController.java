package com.ronda.api.controller;

import com.ronda.api.dto.request.BusquedaGuardadaRequestDto;
import com.ronda.api.dto.response.BusquedaGuardadaResponseDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.service.BusquedaGuardadaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/busquedas-guardadas")
@RequiredArgsConstructor
public class BusquedaGuardadaController {

    private final BusquedaGuardadaService busquedaGuardadaService;

    @GetMapping
    public ResponseEntity<List<BusquedaGuardadaResponseDto>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(busquedaGuardadaService.listar(usuario));
    }

    @PostMapping
    public ResponseEntity<BusquedaGuardadaResponseDto> crear(@AuthenticationPrincipal Usuario usuario,
                                                               @RequestBody BusquedaGuardadaRequestDto dto) {
        return ResponseEntity.status(201).body(busquedaGuardadaService.crear(usuario, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        busquedaGuardadaService.eliminar(usuario, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/revisada")
    public ResponseEntity<Void> marcarRevisada(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        busquedaGuardadaService.marcarRevisada(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
