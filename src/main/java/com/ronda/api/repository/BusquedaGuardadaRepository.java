package com.ronda.api.repository;

import com.ronda.api.entity.BusquedaGuardada;
import com.ronda.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusquedaGuardadaRepository extends JpaRepository<BusquedaGuardada, Long> {
    List<BusquedaGuardada> findByUsuario(Usuario usuario);
}
