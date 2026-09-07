package com.ronda.api.repository;

import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {
    List<Publicacion> findByVendedorAndEstado(Usuario vendedor, EstadoPublicacion estado);
    List<Publicacion> findByVendedor(Usuario vendedor);
    Optional<Publicacion> findByVendedorAndEstadoOrderByFechaActualizacionDesc(Usuario vendedor, EstadoPublicacion estado);
    List<Publicacion> findByVendedorAndEstadoIn(Usuario vendedor, List<EstadoPublicacion> estados);
}
