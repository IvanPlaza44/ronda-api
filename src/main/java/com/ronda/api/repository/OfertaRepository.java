package com.ronda.api.repository;

import com.ronda.api.entity.Oferta;
import com.ronda.api.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {
    List<Oferta> findByPublicacionOrderByFechaDesc(Publicacion publicacion);
}
