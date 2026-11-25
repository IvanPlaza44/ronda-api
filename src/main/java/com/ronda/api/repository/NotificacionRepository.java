package com.ronda.api.repository;

import com.ronda.api.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    // Avisos de un usuario que todavía no se le entregaron, del más viejo al más nuevo.
    List<Notificacion> findByUsuarioIdAndEntregadaFalseOrderByIdAsc(Long usuarioId);
}