package com.ronda.api.controller;

import com.ronda.api.dto.response.CategoriaDto;
import com.ronda.api.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public ResponseEntity<List<CategoriaDto>> listar() {
        List<CategoriaDto> categorias = categoriaRepository.findAll().stream()
                .map(c -> new CategoriaDto(c.getId(), c.getNombre()))
                .toList();
        return ResponseEntity.ok(categorias);
    }
}
