package com.ronda.api.service;

import com.ronda.api.dto.request.ActualizarPerfilDto;
import com.ronda.api.dto.response.PerfilPublicoResponseDto;
import com.ronda.api.dto.response.PerfilResponseDto;
import com.ronda.api.dto.response.PublicacionResumenDto;
import com.ronda.api.entity.Calificacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoPublicacion;
import com.ronda.api.enums.RolCalificacion;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.CalificacionRepository;
import com.ronda.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CalificacionRepository calificacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public PerfilResponseDto obtenerPerfilPropio(Usuario usuario) {
        List<Calificacion> recibidas = calificacionRepository.findByReceptor(usuario);
        double promedio = recibidas.stream().mapToInt(Calificacion::getEstrellas).average().orElse(0.0);
        long comoComprador = recibidas.stream().filter(c -> c.getRolReceptor() == RolCalificacion.COMPRADOR).count();
        long comoVendedor = recibidas.stream().filter(c -> c.getRolReceptor() == RolCalificacion.VENDEDOR).count();

        return new PerfilResponseDto(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getUsername(),
                usuario.getTelefono(), usuario.getZona(), usuario.getFotoPerfil(), usuario.getFechaAlta(),
                Math.round(promedio * 10.0) / 10.0, comoComprador, comoVendedor
        );
    }

    @Transactional
    public PerfilResponseDto actualizarFotoPerfil(Usuario usuario, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw ApiException.solicitudInvalida("Debe adjuntar una imagen");
        }
        String url = cloudinaryService.subirImagen(archivo, "ronda/perfiles");
        usuario.setFotoPerfil(url);
        usuarioRepository.save(usuario);
        return obtenerPerfilPropio(usuario);
    }

    @Transactional
    public PerfilResponseDto actualizarPerfil(Usuario usuario, ActualizarPerfilDto dto) {
        if (dto.nombre() != null) usuario.setNombre(dto.nombre());
        if (dto.telefono() != null) usuario.setTelefono(dto.telefono());
        if (dto.zona() != null) usuario.setZona(dto.zona());
        if (dto.username() != null) {
            if (!dto.username().equals(usuario.getUsername()) && usuarioRepository.existsByUsername(dto.username())) {
                throw ApiException.conflicto("El nombre de usuario ya está en uso");
            }
            usuario.setUsername(dto.username());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.password()));
        }
        usuarioRepository.save(usuario);
        return obtenerPerfilPropio(usuario);
    }

    public PerfilPublicoResponseDto obtenerPerfilPublico(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));

        List<Calificacion> recibidas = calificacionRepository.findByReceptor(usuario);
        double promedio = recibidas.stream().mapToInt(Calificacion::getEstrellas).average().orElse(0.0);

        List<PublicacionResumenDto> activas = usuario.getPublicaciones().stream()
                .filter(p -> p.getEstado() == EstadoPublicacion.ACTIVA)
                .map(p -> new PublicacionResumenDto(
                        p.getId(), p.getTitulo(), p.getPrecio(), p.getEstadoArticulo(), p.getEstado(),
                        p.getZonaEntrega(), p.getFotos().isEmpty() ? null : p.getFotos().get(0).getUrl(),
                        p.getFechaPublicacion(), usuario.getNombre()))
                .toList();

        return new PerfilPublicoResponseDto(
                usuario.getId(), usuario.getNombre(), usuario.getZona(), usuario.getFotoPerfil(), usuario.getFechaAlta(),
                Math.round(promedio * 10.0) / 10.0, recibidas.size(), activas
        );
    }
}