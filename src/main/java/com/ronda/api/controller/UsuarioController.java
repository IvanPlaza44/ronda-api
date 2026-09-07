package com.ronda.api.controller;

import com.ronda.api.dto.request.ActualizarPerfilDto;
import com.ronda.api.dto.request.CalificacionRequestDto;
import com.ronda.api.dto.response.PerfilPublicoResponseDto;
import com.ronda.api.dto.response.PerfilResponseDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.service.CalificacionService;
import com.ronda.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final CalificacionService calificacionService;

    @GetMapping("/me")
    public ResponseEntity<PerfilResponseDto> perfilPropio(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPropio(usuario));
    }

    @PutMapping("/me")
    public ResponseEntity<PerfilResponseDto> actualizarPerfil(@AuthenticationPrincipal Usuario usuario,
                                                                @RequestBody ActualizarPerfilDto dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(usuario, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilPublicoResponseDto> perfilPublico(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPublico(id));
    }

    @PostMapping("/{id}/calificaciones")
    public ResponseEntity<Void> calificar(@AuthenticationPrincipal Usuario emisor,
                                           @PathVariable Long id,
                                           @RequestBody CalificacionRequestDto dto) {
        calificacionService.calificar(emisor, id, dto);
        return ResponseEntity.status(201).build();
    }
}
