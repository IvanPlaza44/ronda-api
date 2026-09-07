package com.ronda.api.service;

import com.ronda.api.dto.request.CalificacionRequestDto;
import com.ronda.api.entity.Calificacion;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.CalificacionRepository;
import com.ronda.api.repository.PublicacionRepository;
import com.ronda.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;

    public void calificar(Usuario emisor, Long receptorId, CalificacionRequestDto dto) {
        if (dto.estrellas() == null || dto.estrellas() < 1 || dto.estrellas() > 5) {
            throw ApiException.solicitudInvalida("Las estrellas deben estar entre 1 y 5");
        }
        if (receptorId.equals(emisor.getId())) {
            throw ApiException.solicitudInvalida("No podés calificarte a vos mismo");
        }

        Usuario receptor = usuarioRepository.findById(receptorId)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario a calificar no encontrado"));

        Publicacion publicacion = null;
        if (dto.publicacionId() != null) {
            publicacion = publicacionRepository.findById(dto.publicacionId())
                    .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        }

        Calificacion calificacion = Calificacion.builder()
                .emisor(emisor)
                .receptor(receptor)
                .publicacion(publicacion)
                .rolReceptor(dto.rolReceptor())
                .estrellas(dto.estrellas())
                .comentario(dto.comentario())
                .build();

        calificacionRepository.save(calificacion);
    }
}
