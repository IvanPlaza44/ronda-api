package com.ronda.api.service;

import com.ronda.api.dto.request.PublicacionRequestDto;
import com.ronda.api.dto.response.PaginaDto;
import com.ronda.api.dto.response.PublicacionDetalleDto;
import com.ronda.api.dto.response.PublicacionResumenDto;
import com.ronda.api.entity.Calificacion;
import com.ronda.api.entity.Categoria;
import com.ronda.api.entity.Foto;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoArticulo;
import com.ronda.api.enums.EstadoPublicacion;
import com.ronda.api.enums.OrdenPublicacion;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.CalificacionRepository;
import com.ronda.api.repository.CategoriaRepository;
import com.ronda.api.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final CategoriaRepository categoriaRepository;
    private final CalificacionRepository calificacionRepository;
    private final CloudinaryService cloudinaryService;

    // ---------- Explorar (Home) ----------

    public PaginaDto<PublicacionResumenDto> buscar(String query, Long categoriaId, BigDecimal precioMin,
                                                     BigDecimal precioMax, EstadoArticulo estadoArticulo,
                                                     String zona, OrdenPublicacion orden, int pagina, int tamanio) {

        Specification<Publicacion> spec = Specification.where(PublicacionSpecifications.estado(EstadoPublicacion.ACTIVA))
                .and(PublicacionSpecifications.texto(query))
                .and(PublicacionSpecifications.categoria(categoriaId))
                .and(PublicacionSpecifications.precioMinimo(precioMin))
                .and(PublicacionSpecifications.precioMaximo(precioMax))
                .and(PublicacionSpecifications.estadoArticulo(estadoArticulo))
                .and(PublicacionSpecifications.zona(zona));

        Sort sort = switch (orden == null ? OrdenPublicacion.RECIENTES : orden) {
            case MENOR_PRECIO -> Sort.by(Sort.Direction.ASC, "precio");
            case MAYOR_PRECIO -> Sort.by(Sort.Direction.DESC, "precio");
            case RECIENTES -> Sort.by(Sort.Direction.DESC, "fechaPublicacion");
        };

        Page<Publicacion> page = publicacionRepository.findAll(spec, PageRequest.of(pagina, tamanio, sort));
        List<PublicacionResumenDto> contenido = page.getContent().stream().map(this::aResumen).toList();

        return new PaginaDto<>(contenido, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    // ---------- Detalle ----------

    public PublicacionDetalleDto obtenerDetalle(Long id, Usuario usuarioActual) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));

        double promedio = calificacionRepository.findByReceptor(p.getVendedor()).stream()
                .mapToInt(Calificacion::getEstrellas).average().orElse(0.0);

        boolean esPropia = usuarioActual != null && usuarioActual.getId().equals(p.getVendedor().getId());

        return new PublicacionDetalleDto(
                p.getId(), p.getTitulo(), p.getDescripcion(),
                p.getCategoria() != null ? p.getCategoria().getNombre() : null,
                p.getPrecio(), p.getEstadoArticulo(), p.getEstado(), p.getZonaEntrega(), p.getFechaPublicacion(),
                p.getFotos().stream().map(Foto::getUrl).toList(),
                p.getVendedor().getId(), p.getVendedor().getNombre(),
                Math.round(promedio * 10.0) / 10.0, esPropia
        );
    }

    // ---------- Publicar (carga guiada / borrador) ----------

    @Transactional
    public PublicacionDetalleDto obtenerOCrearBorrador(Usuario vendedor) {
        return publicacionRepository.findByVendedorAndEstadoOrderByFechaActualizacionDesc(vendedor, EstadoPublicacion.BORRADOR)
                .map(p -> obtenerDetalle(p.getId(), vendedor))
                .orElseGet(() -> {
                    Publicacion nueva = Publicacion.builder()
                            .vendedor(vendedor)
                            .estado(EstadoPublicacion.BORRADOR)
                            .fechaActualizacion(LocalDateTime.now())
                            .build();
                    publicacionRepository.save(nueva);
                    return obtenerDetalle(nueva.getId(), vendedor);
                });
    }

    @Transactional
    public PublicacionDetalleDto guardarPaso(Usuario vendedor, Long id, PublicacionRequestDto dto) {
        Publicacion p = obtenerPropia(vendedor, id);
        aplicarCambios(p, dto);
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
        return obtenerDetalle(p.getId(), vendedor);
    }

    @Transactional
    public PublicacionDetalleDto publicar(Usuario vendedor, Long id) {
        Publicacion p = obtenerPropia(vendedor, id);

        if (p.getTitulo() == null || p.getPrecio() == null || p.getCategoria() == null || p.getEstadoArticulo() == null) {
            throw ApiException.solicitudInvalida("Faltan datos obligatorios para publicar (título, categoría, precio, estado del artículo)");
        }

        p.setEstado(EstadoPublicacion.ACTIVA);
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
        return obtenerDetalle(p.getId(), vendedor);
    }

    @Transactional
    public PublicacionDetalleDto agregarFoto(Usuario vendedor, Long id, MultipartFile archivo) {
        Publicacion p = obtenerPropia(vendedor, id);
        // Fuerza la carga de la lista (lazy) ANTES de agregar: si se agrega sobre una
        // lista todavia no inicializada, Hibernate no calcula bien el indice de la
        // columna @OrderColumn ("orden") y lo guarda en null, rompiendo la lectura posterior.
        p.getFotos().size();

        String url = cloudinaryService.subirImagen(archivo);
        Foto foto = Foto.builder().publicacion(p).url(url).build();
        p.getFotos().add(foto);
        p.setFechaActualizacion(LocalDateTime.now());
        publicacionRepository.save(p);
        return obtenerDetalle(p.getId(), vendedor);
    }

    public List<PublicacionResumenDto> misPublicaciones(Usuario vendedor, EstadoPublicacion estado) {
        List<Publicacion> lista = estado == null
                ? publicacionRepository.findByVendedorAndEstadoIn(vendedor, List.of(EstadoPublicacion.ACTIVA, EstadoPublicacion.PAUSADA, EstadoPublicacion.VENDIDA))
                : publicacionRepository.findByVendedorAndEstado(vendedor, estado);
        return lista.stream().map(this::aResumen).toList();
    }

    @Transactional
    public void pausar(Usuario vendedor, Long id) {
        Publicacion p = obtenerPropia(vendedor, id);
        if (p.getEstado() != EstadoPublicacion.ACTIVA) {
            throw ApiException.solicitudInvalida("Solo se puede pausar una publicación activa");
        }
        p.setEstado(EstadoPublicacion.PAUSADA);
        publicacionRepository.save(p);
    }

    @Transactional
    public void reactivar(Usuario vendedor, Long id) {
        Publicacion p = obtenerPropia(vendedor, id);
        if (p.getEstado() != EstadoPublicacion.PAUSADA) {
            throw ApiException.solicitudInvalida("Solo se puede reactivar una publicación pausada");
        }
        p.setEstado(EstadoPublicacion.ACTIVA);
        publicacionRepository.save(p);
    }

    @Transactional
    public void marcarVendida(Usuario vendedor, Long id) {
        Publicacion p = obtenerPropia(vendedor, id);
        p.setEstado(EstadoPublicacion.VENDIDA);
        publicacionRepository.save(p);
    }

    // ---------- Helpers ----------

    private Publicacion obtenerPropia(Usuario vendedor, Long id) {
        Publicacion p = publicacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));
        if (!p.getVendedor().getId().equals(vendedor.getId())) {
            throw ApiException.noAutorizado("No podés modificar una publicación que no es tuya");
        }
        return p;
    }

    private void aplicarCambios(Publicacion p, PublicacionRequestDto dto) {
        if (dto.titulo() != null) p.setTitulo(dto.titulo());
        if (dto.descripcion() != null) p.setDescripcion(dto.descripcion());
        if (dto.precio() != null) p.setPrecio(dto.precio());
        if (dto.estadoArticulo() != null) p.setEstadoArticulo(dto.estadoArticulo());
        if (dto.zonaEntrega() != null) p.setZonaEntrega(dto.zonaEntrega());
        if (dto.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> ApiException.solicitudInvalida("Categoría inválida"));
            p.setCategoria(categoria);
        }
        if (dto.fotosUrls() != null) {
            p.getFotos().clear();
            List<Foto> fotos = new ArrayList<>();
            for (String url : dto.fotosUrls()) {
                fotos.add(Foto.builder().publicacion(p).url(url).build());
            }
            p.getFotos().addAll(fotos);
        }
    }

    private PublicacionResumenDto aResumen(Publicacion p) {
        return new PublicacionResumenDto(
                p.getId(), p.getTitulo(), p.getPrecio(), p.getEstadoArticulo(), p.getEstado(),
                p.getZonaEntrega(), p.getFotos().isEmpty() ? null : p.getFotos().get(0).getUrl(),
                p.getFechaPublicacion(), p.getVendedor().getNombre()
        );
    }
}
