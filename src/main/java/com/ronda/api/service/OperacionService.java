package com.ronda.api.service;

import com.ronda.api.dto.request.PuntoEncuentroRequestDto;
import com.ronda.api.dto.response.OperacionResponseDto;
import com.ronda.api.entity.Operacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoOperacion;
import com.ronda.api.enums.EstadoPublicacion;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.CalificacionRepository;
import com.ronda.api.repository.OperacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperacionService {

    private static final int DIAS_VENTANA_CALIFICACION = 7;

    private final OperacionRepository operacionRepository;
    private final CalificacionRepository calificacionRepository;

    public OperacionResponseDto obtenerDetalle(Usuario usuario, Long operacionId) {
        Operacion operacion = obtenerYValidarParticipante(usuario, operacionId);
        return aDto(operacion, usuario);
    }

    public List<OperacionResponseDto> listarMias(Usuario usuario) {
        List<Operacion> propias = new ArrayList<>();
        propias.addAll(operacionRepository.findByCompradorOrderByFechaAcordadaDesc(usuario));
        propias.addAll(operacionRepository.findByVendedorOrderByFechaAcordadaDesc(usuario));
        return propias.stream()
                .sorted(Comparator.comparing(Operacion::getFechaAcordada).reversed())
                .map(op -> aDto(op, usuario))
                .toList();
    }

    @Transactional
    public OperacionResponseDto actualizarPuntoEncuentro(Usuario usuario, Long operacionId, PuntoEncuentroRequestDto dto) {
        Operacion operacion = obtenerYValidarParticipante(usuario, operacionId);
        if (operacion.getEstado() == EstadoOperacion.CANCELADA) {
            throw ApiException.conflicto("La operación está cancelada");
        }
        operacion.setDireccionEncuentro(dto.direccion());
        operacion.setLatitudEncuentro(dto.latitud());
        operacion.setLongitudEncuentro(dto.longitud());
        operacionRepository.save(operacion);
        return aDto(operacion, usuario);
    }

    @Transactional
    public OperacionResponseDto marcarEntregada(Usuario usuario, Long operacionId) {
        Operacion operacion = obtenerYValidarParticipante(usuario, operacionId);
        if (operacion.getEstado() == EstadoOperacion.ENTREGADA) {
            throw ApiException.conflicto("La operación ya fue marcada como entregada");
        }
        if (operacion.getEstado() == EstadoOperacion.CANCELADA) {
            throw ApiException.conflicto("La operación está cancelada");
        }
        operacion.setEstado(EstadoOperacion.ENTREGADA);
        operacion.setFechaEntrega(LocalDateTime.now());
        operacionRepository.save(operacion);

        operacion.getPublicacion().setEstado(EstadoPublicacion.VENDIDA);

        return aDto(operacion, usuario);
    }

    boolean puedeCalificar(Operacion operacion, Usuario usuario) {
        if (operacion.getEstado() != EstadoOperacion.ENTREGADA || operacion.getFechaEntrega() == null) {
            return false;
        }
        boolean dentroDeVentana = operacion.getFechaEntrega().plusDays(DIAS_VENTANA_CALIFICACION).isAfter(LocalDateTime.now());
        boolean yaCalifico = calificacionRepository.existsByOperacionAndEmisor(operacion, usuario);
        return dentroDeVentana && !yaCalifico;
    }

    private Operacion obtenerYValidarParticipante(Usuario usuario, Long operacionId) {
        Operacion operacion = operacionRepository.findById(operacionId)
                .orElseThrow(() -> ApiException.noEncontrado("Operación no encontrada"));
        boolean esParticipante = operacion.getComprador().getId().equals(usuario.getId())
                || operacion.getVendedor().getId().equals(usuario.getId());
        if (!esParticipante) {
            throw ApiException.noAutorizado("No participás de esta operación");
        }
        return operacion;
    }

    private OperacionResponseDto aDto(Operacion o, Usuario visor) {
        String tipo = o.getComprador().getId().equals(visor.getId()) ? "COMPRA" : "VENTA";
        return new OperacionResponseDto(
                o.getId(),
                o.getPublicacion().getId(),
                o.getPublicacion().getTitulo(),
                o.getComprador().getId(),
                o.getComprador().getNombre(),
                o.getVendedor().getId(),
                o.getVendedor().getNombre(),
                o.getMontoFinal(),
                o.getEstado().name(),
                o.getFechaAcordada(),
                o.getFechaEntrega(),
                o.getDireccionEncuentro(),
                o.getLatitudEncuentro(),
                o.getLongitudEncuentro(),
                puedeCalificar(o, visor),
                tipo
        );
    }
}
