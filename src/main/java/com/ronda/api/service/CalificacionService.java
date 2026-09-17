package com.ronda.api.service;

import com.ronda.api.dto.request.CalificacionOperacionRequestDto;
import com.ronda.api.dto.request.CalificacionRequestDto;
import com.ronda.api.entity.Calificacion;
import com.ronda.api.entity.Operacion;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoOperacion;
import com.ronda.api.enums.RolCalificacion;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.CalificacionRepository;
import com.ronda.api.repository.OperacionRepository;
import com.ronda.api.repository.PublicacionRepository;
import com.ronda.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CalificacionService {

    private static final int DIAS_VENTANA_CALIFICACION = 7;

    private final CalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final OperacionRepository operacionRepository;

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

    /** Calificación ligada a una operación concreta (entrega), respetando la ventana de 7 días desde la entrega. */
    @Transactional
    public void calificarPorOperacion(Usuario emisor, Long operacionId, CalificacionOperacionRequestDto dto) {
        if (dto.estrellas() == null || dto.estrellas() < 1 || dto.estrellas() > 5) {
            throw ApiException.solicitudInvalida("Las estrellas deben estar entre 1 y 5");
        }

        Operacion operacion = operacionRepository.findById(operacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Operación no encontrada"));

        boolean esComprador = operacion.getComprador().getId().equals(emisor.getId());
        boolean esVendedor = operacion.getVendedor().getId().equals(emisor.getId());
        if (!esComprador && !esVendedor) {
            throw ApiException.noAutorizado("No participás de esta operación");
        }

        if (operacion.getEstado() != EstadoOperacion.ENTREGADA || operacion.getFechaEntrega() == null) {
            throw ApiException.solicitudInvalida("Solo se puede calificar una operación ya entregada");
        }

        if (operacion.getFechaEntrega().plusDays(DIAS_VENTANA_CALIFICACION).isBefore(LocalDateTime.now())) {
            throw ApiException.conflicto("La ventana de 7 días para calificar esta operación ya venció");
        }

        if (calificacionRepository.existsByOperacionAndEmisor(operacion, emisor)) {
            throw ApiException.conflicto("Ya calificaste esta operación");
        }

        Usuario receptor = esComprador ? operacion.getVendedor() : operacion.getComprador();
        RolCalificacion rolReceptor = esComprador ? RolCalificacion.VENDEDOR : RolCalificacion.COMPRADOR;

        Calificacion calificacion = Calificacion.builder()
                .emisor(emisor)
                .receptor(receptor)
                .publicacion(operacion.getPublicacion())
                .operacion(operacion)
                .rolReceptor(rolReceptor)
                .estrellas(dto.estrellas())
                .comentario(dto.comentario())
                .build();

        calificacionRepository.save(calificacion);
    }
}
