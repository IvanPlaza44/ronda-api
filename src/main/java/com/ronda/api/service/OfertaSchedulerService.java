package com.ronda.api.service;

import com.ronda.api.entity.Oferta;
import com.ronda.api.enums.EstadoOferta;
import com.ronda.api.repository.OfertaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Vence automáticamente las ofertas pendientes cuya fecha de vencimiento ya pasó. */
@Service
@RequiredArgsConstructor
@Slf4j
public class OfertaSchedulerService {

    private final OfertaRepository ofertaRepository;

    // Corre cada 15 minutos
    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Transactional
    public void vencerOfertasExpiradas() {
        List<Oferta> vencidas = ofertaRepository.findByEstadoAndFechaVencimientoBefore(EstadoOferta.PENDIENTE, LocalDateTime.now());
        if (vencidas.isEmpty()) {
            return;
        }
        vencidas.forEach(o -> o.setEstado(EstadoOferta.VENCIDA));
        ofertaRepository.saveAll(vencidas);
        log.info("Se vencieron {} ofertas automáticamente", vencidas.size());
    }
}
