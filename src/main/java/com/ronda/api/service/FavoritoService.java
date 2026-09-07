package com.ronda.api.service;

import com.ronda.api.dto.response.FavoritoResponseDto;
import com.ronda.api.dto.response.PublicacionResumenDto;
import com.ronda.api.entity.Favorito;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.FavoritoRepository;
import com.ronda.api.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final PublicacionRepository publicacionRepository;

    @Transactional
    public void agregar(Usuario usuario, Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));

        if (favoritoRepository.existsByUsuarioAndPublicacion(usuario, publicacion)) return;

        favoritoRepository.save(Favorito.builder()
                .usuario(usuario)
                .publicacion(publicacion)
                .ultimoPrecioVisto(publicacion.getPrecio())
                .build());
    }

    @Transactional
    public void quitar(Usuario usuario, Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        favoritoRepository.findByUsuarioAndPublicacion(usuario, publicacion)
                .ifPresent(favoritoRepository::delete);
    }

    public List<FavoritoResponseDto> listar(Usuario usuario) {
        List<Favorito> favoritos = favoritoRepository.findByUsuario(usuario);
        return favoritos.stream().map(f -> {
            Publicacion p = f.getPublicacion();
            boolean cambioPrecio = f.getUltimoPrecioVisto() != null && p.getPrecio() != null
                    && f.getUltimoPrecioVisto().compareTo(p.getPrecio()) != 0;

            PublicacionResumenDto resumen = new PublicacionResumenDto(
                    p.getId(), p.getTitulo(), p.getPrecio(), p.getEstadoArticulo(), p.getEstado(),
                    p.getZonaEntrega(), p.getFotos().isEmpty() ? null : p.getFotos().get(0).getUrl(),
                    p.getFechaPublicacion(), p.getVendedor().getNombre());

            return new FavoritoResponseDto(f.getId(), resumen, cambioPrecio);
        }).toList();
    }
}
