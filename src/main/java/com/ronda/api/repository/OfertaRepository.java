package com.ronda.api.repository;

import com.ronda.api.entity.Oferta;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import com.ronda.api.enums.EstadoOferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    List<Oferta> findByPublicacionOrderByFechaDesc(Publicacion publicacion);

    List<Oferta> findByAutorOrderByFechaDesc(Usuario autor);

    @Query("SELECT o FROM Oferta o WHERE o.publicacion.vendedor = :vendedor ORDER BY o.fecha DESC")
    List<Oferta> findRecibidasPorVendedor(@Param("vendedor") Usuario vendedor);

    List<Oferta> findByEstadoAndFechaVencimientoBefore(EstadoOferta estado, LocalDateTime momento);
}
