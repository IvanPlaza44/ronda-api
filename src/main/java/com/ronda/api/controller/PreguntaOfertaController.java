package com.ronda.api.controller;

import com.ronda.api.dto.request.OfertaRequestDto;
import com.ronda.api.dto.request.PreguntaRequestDto;
import com.ronda.api.dto.request.RespuestaPreguntaDto;
import com.ronda.api.dto.response.OfertaResponseDto;
import com.ronda.api.dto.response.PreguntaResponseDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.service.PreguntaOfertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones/{publicacionId}")
@RequiredArgsConstructor
public class PreguntaOfertaController {

    private final PreguntaOfertaService preguntaOfertaService;

    @PostMapping("/preguntas")
    public ResponseEntity<PreguntaResponseDto> preguntar(@AuthenticationPrincipal Usuario usuario,
                                                           @PathVariable Long publicacionId,
                                                           @RequestBody PreguntaRequestDto dto) {
        return ResponseEntity.status(201).body(preguntaOfertaService.preguntar(usuario, publicacionId, dto));
    }

    @GetMapping("/preguntas")
    public ResponseEntity<List<PreguntaResponseDto>> listarPreguntas(@PathVariable Long publicacionId) {
        return ResponseEntity.ok(preguntaOfertaService.listarPreguntas(publicacionId));
    }

    @PutMapping("/preguntas/{preguntaId}/respuesta")
    public ResponseEntity<PreguntaResponseDto> responder(@AuthenticationPrincipal Usuario usuario,
                                                           @PathVariable Long publicacionId,
                                                           @PathVariable Long preguntaId,
                                                           @RequestBody RespuestaPreguntaDto dto) {
        return ResponseEntity.ok(preguntaOfertaService.responder(usuario, publicacionId, preguntaId, dto));
    }

    @PostMapping("/ofertas")
    public ResponseEntity<OfertaResponseDto> ofertar(@AuthenticationPrincipal Usuario usuario,
                                                       @PathVariable Long publicacionId,
                                                       @RequestBody OfertaRequestDto dto) {
        return ResponseEntity.status(201).body(preguntaOfertaService.ofertar(usuario, publicacionId, dto));
    }

    @GetMapping("/ofertas")
    public ResponseEntity<List<OfertaResponseDto>> listarOfertas(@AuthenticationPrincipal Usuario usuario,
                                                                   @PathVariable Long publicacionId) {
        return ResponseEntity.ok(preguntaOfertaService.listarOfertas(usuario, publicacionId));
    }
}
