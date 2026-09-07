package com.ronda.api.service;

import com.ronda.api.dto.request.OfertaRequestDto;
import com.ronda.api.dto.request.PreguntaRequestDto;
import com.ronda.api.dto.request.RespuestaPreguntaDto;
import com.ronda.api.dto.response.OfertaResponseDto;
import com.ronda.api.dto.response.PreguntaResponseDto;
import com.ronda.api.entity.Oferta;
import com.ronda.api.entity.Pregunta;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.OfertaRepository;
import com.ronda.api.repository.PreguntaRepository;
import com.ronda.api.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PreguntaOfertaService {

    private final PreguntaRepository preguntaRepository;
    private final OfertaRepository ofertaRepository;
    private final PublicacionRepository publicacionRepository;

    @Transactional
    public PreguntaResponseDto preguntar(Usuario autor, Long publicacionId, PreguntaRequestDto dto) {
        Publicacion publicacion = obtenerPublicacion(publicacionId);
        if (publicacion.getVendedor().getId().equals(autor.getId())) {
            throw ApiException.solicitudInvalida("No podés preguntarle al vendedor de tu propia publicación");
        }
        Pregunta pregunta = Pregunta.builder().publicacion(publicacion).autor(autor).mensaje(dto.mensaje()).build();
        preguntaRepository.save(pregunta);
        return aDto(pregunta);
    }

    public List<PreguntaResponseDto> listarPreguntas(Long publicacionId) {
        Publicacion publicacion = obtenerPublicacion(publicacionId);
        return preguntaRepository.findByPublicacionOrderByFechaAsc(publicacion).stream().map(this::aDto).toList();
    }

    @Transactional
    public PreguntaResponseDto responder(Usuario vendedor, Long publicacionId, Long preguntaId, RespuestaPreguntaDto dto) {
        Publicacion publicacion = obtenerPublicacion(publicacionId);
        if (!publicacion.getVendedor().getId().equals(vendedor.getId())) {
            throw ApiException.noAutorizado("Solo el vendedor puede responder preguntas de su publicación");
        }
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
                .orElseThrow(() -> ApiException.noEncontrado("Pregunta no encontrada"));
        pregunta.setRespuesta(dto.respuesta());
        preguntaRepository.save(pregunta);
        return aDto(pregunta);
    }

    @Transactional
    public OfertaResponseDto ofertar(Usuario autor, Long publicacionId, OfertaRequestDto dto) {
        Publicacion publicacion = obtenerPublicacion(publicacionId);
        if (publicacion.getVendedor().getId().equals(autor.getId())) {
            throw ApiException.solicitudInvalida("No podés ofertar en tu propia publicación");
        }
        if (dto.monto() == null || dto.monto().signum() <= 0) {
            throw ApiException.solicitudInvalida("El monto de la oferta debe ser mayor a cero");
        }
        Oferta oferta = Oferta.builder().publicacion(publicacion).autor(autor).monto(dto.monto()).build();
        ofertaRepository.save(oferta);
        return aDto(oferta);
    }

    public List<OfertaResponseDto> listarOfertas(Usuario solicitante, Long publicacionId) {
        Publicacion publicacion = obtenerPublicacion(publicacionId);
        if (!publicacion.getVendedor().getId().equals(solicitante.getId())) {
            throw ApiException.noAutorizado("Solo el vendedor puede ver las ofertas recibidas");
        }
        return ofertaRepository.findByPublicacionOrderByFechaDesc(publicacion).stream().map(this::aDto).toList();
    }

    private Publicacion obtenerPublicacion(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
    }

    private PreguntaResponseDto aDto(Pregunta p) {
        return new PreguntaResponseDto(p.getId(), p.getAutor().getNombre(), p.getMensaje(), p.getRespuesta(), p.getFecha());
    }

    private OfertaResponseDto aDto(Oferta o) {
        return new OfertaResponseDto(o.getId(), o.getAutor().getNombre(), o.getMonto(), o.getEstado(), o.getFecha());
    }
}
