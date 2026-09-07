package com.ronda.api.controller;

import com.ronda.api.dto.response.FavoritoResponseDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.service.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    @GetMapping
    public ResponseEntity<List<FavoritoResponseDto>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(favoritoService.listar(usuario));
    }

    @PostMapping("/{publicacionId}")
    public ResponseEntity<Void> agregar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long publicacionId) {
        favoritoService.agregar(usuario, publicacionId);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{publicacionId}")
    public ResponseEntity<Void> quitar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long publicacionId) {
        favoritoService.quitar(usuario, publicacionId);
        return ResponseEntity.noContent().build();
    }
}
