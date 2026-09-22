package com.ronda.api.service;

import com.ronda.api.dto.request.ContraofertaRequestDto;
import com.ronda.api.dto.request.OfertaRequestDto;
import com.ronda.api.dto.request.PreguntaRequestDto;
import com.ronda.api.dto.request.RespuestaPreguntaDto;
import com.ronda.api.dto.response.OfertaResponseDto;
import com.ronda.api.dto.response.PreguntaResponseDto;
import com.ronda.api.entity.Oferta;
import com.ronda.api.entity.Operacion;
import com.ronda.api.entity.Pregunta;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoOferta;
import com.ronda.api.enums.EstadoOperacion;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.OfertaRepository;
import com.ronda.api.repository.OperacionRepository;
import com.ronda.api.repository.PreguntaRepository;
import com.ronda.api.repository.PublicacionRepository;
import com.ronda.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreguntaOfertaService {

    private final PreguntaRepository preguntaRepository;
    private final OfertaRepository ofertaRepository;
    private final PublicacionRepository publicacionRepository;
    private final OperacionRepository operacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.ofertas.vigencia-horas:48}")
    private int vigenciaHoras;

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

    @Transactional(readOnly = true)
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
        validarMonto(dto.monto());
        Usuario autorManaged = usuarioRepository.getReferenceById(autor.getId());
        Oferta oferta = Oferta.builder()
                .publicacion(publicacion)
                .autor(autorManaged)
                .monto(dto.monto())
                .mensaje(dto.mensaje())
                .estado(EstadoOferta.PENDIENTE)
                .fechaVencimiento(LocalDateTime.now().plusHours(vigenciaHoras))
                .build();
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

    @Transactional
    public OfertaResponseDto aceptarOferta(Usuario usuario, Long ofertaId) {
        Oferta oferta = obtenerOfertaVigente(ofertaId);
        validarAutorizadoARepondar(usuario, oferta);

        oferta.setEstado(EstadoOferta.ACEPTADA);
        ofertaRepository.save(oferta);

        Usuario comprador = obtenerComprador(oferta);
        Usuario vendedor = oferta.getPublicacion().getVendedor();

        Operacion operacion = Operacion.builder()
                .publicacion(oferta.getPublicacion())
                .comprador(comprador)
                .vendedor(vendedor)
                .montoFinal(oferta.getMonto())
                .estado(EstadoOperacion.PENDIENTE_ENTREGA)
                .fechaAcordada(LocalDateTime.now())
                .build();
        operacionRepository.save(operacion);

        return aDto(oferta);
    }

    @Transactional
    public OfertaResponseDto rechazarOferta(Usuario usuario, Long ofertaId) {
        Oferta oferta = obtenerOfertaVigente(ofertaId);
        validarAutorizadoARepondar(usuario, oferta);
        oferta.setEstado(EstadoOferta.RECHAZADA);
        ofertaRepository.save(oferta);
        return aDto(oferta);
    }

    @Transactional
    public OfertaResponseDto contraofertar(Usuario usuario, Long ofertaId, ContraofertaRequestDto dto) {
        Oferta original = obtenerOfertaVigente(ofertaId);
        validarAutorizadoARepondar(usuario, original);
        validarMonto(dto.monto());

        original.setEstado(EstadoOferta.CONTRAOFERTADA);
        ofertaRepository.save(original);

        Oferta contraoferta = Oferta.builder()
                .publicacion(original.getPublicacion())
                .autor(usuario)
                .monto(dto.monto())
                .mensaje(dto.mensaje())
                .estado(EstadoOferta.PENDIENTE)
                .fechaVencimiento(LocalDateTime.now().plusHours(vigenciaHoras))
                .ofertaOrigen(original)
                .build();
        ofertaRepository.save(contraoferta);

        return aDto(contraoferta);
    }

    @Transactional(readOnly = true)
    public List<OfertaResponseDto> misOfertasEnviadas(Usuario usuario) {
        return ofertaRepository.findByAutorOrderByFechaDesc(usuario).stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<OfertaResponseDto> misOfertasRecibidas(Usuario usuario) {
        return ofertaRepository.findRecibidasPorVendedor(usuario).stream().map(this::aDto).toList();
    }

    // --- helpers de negociación ---

    private Oferta obtenerOfertaVigente(Long ofertaId) {
        Oferta oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> ApiException.noEncontrado("Oferta no encontrada"));

        if (oferta.getEstado() == EstadoOferta.PENDIENTE
                && oferta.getFechaVencimiento() != null
                && oferta.getFechaVencimiento().isBefore(LocalDateTime.now())) {
            oferta.setEstado(EstadoOferta.VENCIDA);
            ofertaRepository.save(oferta);
        }

        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw ApiException.conflicto("La oferta ya no está pendiente (estado actual: " + oferta.getEstado() + ")");
        }
        return oferta;
    }

    /** El comprador es quien inició la cadena de ofertas/contraofertas (autor de la oferta raíz). */
    private Usuario obtenerComprador(Oferta oferta) {
        Oferta actual = oferta;
        while (actual.getOfertaOrigen() != null) {
            actual = actual.getOfertaOrigen();
        }
        return actual.getAutor();
    }

    /** Determina quién debe responder (aceptar/rechazar/contraofertar) una oferta pendiente: siempre la contraparte del autor de esa oferta puntual. */
    private void validarAutorizadoARepondar(Usuario usuario, Oferta oferta) {
        Usuario vendedor = oferta.getPublicacion().getVendedor();
        Usuario comprador = obtenerComprador(oferta);

        Usuario debeResponder = oferta.getAutor().getId().equals(vendedor.getId()) ? comprador : vendedor;

        if (!debeResponder.getId().equals(usuario.getId())) {
            throw ApiException.noAutorizado("No te corresponde responder esta oferta");
        }
    }

    private void validarMonto(java.math.BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw ApiException.solicitudInvalida("El monto de la oferta debe ser mayor a cero");
        }
    }

    private Publicacion obtenerPublicacion(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
    }

    private PreguntaResponseDto aDto(Pregunta p) {
        return new PreguntaResponseDto(p.getId(), p.getAutor().getNombre(), p.getMensaje(), p.getRespuesta(), p.getFecha());
    }

    private OfertaResponseDto aDto(Oferta o) {
        return new OfertaResponseDto(
                o.getId(),
                o.getPublicacion().getId(),
                o.getPublicacion().getTitulo(),
                o.getAutor().getId(),
                o.getAutor().getNombre(),
                o.getMonto(),
                o.getMensaje(),
                o.getEstado().name(),
                o.getFecha(),
                o.getFechaVencimiento(),
                o.getOfertaOrigen() == null ? "OFERTA" : "CONTRAOFERTA"
        );
    }
}
