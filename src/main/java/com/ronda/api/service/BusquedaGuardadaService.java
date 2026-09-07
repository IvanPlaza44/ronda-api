package com.ronda.api.service;

import com.ronda.api.dto.request.BusquedaGuardadaRequestDto;
import com.ronda.api.dto.response.BusquedaGuardadaResponseDto;
import com.ronda.api.entity.BusquedaGuardada;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoPublicacion;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.BusquedaGuardadaRepository;
import com.ronda.api.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusquedaGuardadaService {

    private final BusquedaGuardadaRepository busquedaGuardadaRepository;
    private final PublicacionRepository publicacionRepository;

    @Transactional
    public BusquedaGuardadaResponseDto crear(Usuario usuario, BusquedaGuardadaRequestDto dto) {
        BusquedaGuardada bg = BusquedaGuardada.builder()
                .usuario(usuario)
                .nombre(dto.nombre())
                .query(dto.query())
                .categoriaId(dto.categoriaId())
                .precioMin(dto.precioMin())
                .precioMax(dto.precioMax())
                .estadoArticulo(dto.estadoArticulo())
                .zona(dto.zona())
                .build();
        busquedaGuardadaRepository.save(bg);
        return new BusquedaGuardadaResponseDto(bg.getId(), bg.getNombre(), false);
    }

    public List<BusquedaGuardadaResponseDto> listar(Usuario usuario) {
        return busquedaGuardadaRepository.findByUsuario(usuario).stream()
                .map(bg -> new BusquedaGuardadaResponseDto(bg.getId(), bg.getNombre(), hayNovedades(bg)))
                .toList();
    }

    @Transactional
    public void eliminar(Usuario usuario, Long id) {
        BusquedaGuardada bg = obtenerPropia(usuario, id);
        busquedaGuardadaRepository.delete(bg);
    }

    @Transactional
    public void marcarRevisada(Usuario usuario, Long id) {
        BusquedaGuardada bg = obtenerPropia(usuario, id);
        bg.setUltimaRevision(LocalDateTime.now());
        busquedaGuardadaRepository.save(bg);
    }

    private boolean hayNovedades(BusquedaGuardada bg) {
        Specification<Publicacion> spec = Specification.where(PublicacionSpecifications.estado(EstadoPublicacion.ACTIVA))
                .and(PublicacionSpecifications.texto(bg.getQuery()))
                .and(PublicacionSpecifications.categoria(bg.getCategoriaId()))
                .and(PublicacionSpecifications.precioMinimo(bg.getPrecioMin()))
                .and(PublicacionSpecifications.precioMaximo(bg.getPrecioMax()))
                .and(PublicacionSpecifications.estadoArticulo(bg.getEstadoArticulo()))
                .and(PublicacionSpecifications.zona(bg.getZona()))
                .and((root, query, cb) -> cb.greaterThan(root.get("fechaPublicacion"), bg.getUltimaRevision()));

        return publicacionRepository.count(spec) > 0;
    }

    private BusquedaGuardada obtenerPropia(Usuario usuario, Long id) {
        BusquedaGuardada bg = busquedaGuardadaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Búsqueda guardada no encontrada"));
        if (!bg.getUsuario().getId().equals(usuario.getId())) {
            throw ApiException.noAutorizado("Esta búsqueda guardada no te pertenece");
        }
        return bg;
    }
}
