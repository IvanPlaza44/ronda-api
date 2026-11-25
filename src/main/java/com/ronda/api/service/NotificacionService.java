package com.ronda.api.service;

import com.ronda.api.dto.response.NovedadDto;
import com.ronda.api.entity.Notificacion;
import com.ronda.api.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    /**
     * Guarda un aviso para un usuario.
     * No hace nada si el destinatario es quien hizo la acción.
     */
    public void crear(Long destinatarioId, Long actorId, String tipo, String mensaje,
                      Long publicacionId, Long ofertaId) {
        if (destinatarioId == null || destinatarioId.equals(actorId)) return;

        notificacionRepository.save(Notificacion.builder()
                .usuarioId(destinatarioId)
                .tipo(tipo)
                .mensaje(mensaje)
                .publicacionId(publicacionId)
                .ofertaId(ofertaId)
                .build());
    }

    /** Devuelve los avisos pendientes del usuario y los marca como entregados. */
    @Transactional
    public List<NovedadDto> tomarPendientes(Long usuarioId) {
        List<Notificacion> pendientes =
                notificacionRepository.findByUsuarioIdAndEntregadaFalseOrderByIdAsc(usuarioId);

        pendientes.forEach(n -> n.setEntregada(true));
        notificacionRepository.saveAll(pendientes);

        return pendientes.stream()
                .map(n -> new NovedadDto(n.getId(), n.getTipo(), n.getMensaje(),
                        n.getPublicacionId(), n.getOfertaId()))
                .toList();
    }
}