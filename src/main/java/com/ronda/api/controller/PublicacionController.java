package com.ronda.api.controller;

import com.ronda.api.dto.request.PublicacionRequestDto;
import com.ronda.api.dto.response.PaginaDto;
import com.ronda.api.dto.response.PublicacionDetalleDto;
import com.ronda.api.dto.response.PublicacionResumenDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoArticulo;
import com.ronda.api.enums.EstadoPublicacion;
import com.ronda.api.enums.OrdenPublicacion;
import com.ronda.api.service.PublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    // ---- Explorar (Home) ----
    @GetMapping
    public ResponseEntity<PaginaDto<PublicacionResumenDto>> buscar(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) EstadoArticulo estadoArticulo,
            @RequestParam(required = false) String zona,
            @RequestParam(required = false, defaultValue = "RECIENTES") OrdenPublicacion orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {

        return ResponseEntity.ok(publicacionService.buscar(query, categoriaId, precioMin, precioMax,
                estadoArticulo, zona, orden, pagina, tamanio));
    }

    // ---- Detalle ----
    @GetMapping("/{id}")
    public ResponseEntity<PublicacionDetalleDto> detalle(@PathVariable Long id,
                                                           @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(publicacionService.obtenerDetalle(id, usuario));
    }

    // ---- Publicar: borrador ----
    @GetMapping("/borrador")
    public ResponseEntity<PublicacionDetalleDto> obtenerBorrador(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(publicacionService.obtenerOCrearBorrador(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionDetalleDto> guardarPaso(@AuthenticationPrincipal Usuario usuario,
                                                               @PathVariable Long id,
                                                               @RequestBody PublicacionRequestDto dto) {
        return ResponseEntity.ok(publicacionService.guardarPaso(usuario, id, dto));
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<PublicacionDetalleDto> publicar(@AuthenticationPrincipal Usuario usuario,
                                                            @PathVariable Long id) {
        return ResponseEntity.ok(publicacionService.publicar(usuario, id));
    }

    // ---- Mis publicaciones ----
    @GetMapping("/mias")
    public ResponseEntity<List<PublicacionResumenDto>> misPublicaciones(@AuthenticationPrincipal Usuario usuario,
                                                                          @RequestParam(required = false) EstadoPublicacion estado) {
        return ResponseEntity.ok(publicacionService.misPublicaciones(usuario, estado));
    }

    @PatchMapping("/{id}/pausar")
    public ResponseEntity<Void> pausar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        publicacionService.pausar(usuario, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        publicacionService.reactivar(usuario, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/vendida")
    public ResponseEntity<Void> marcarVendida(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        publicacionService.marcarVendida(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
