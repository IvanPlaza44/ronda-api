package com.ronda.api.repository;

import com.ronda.api.entity.Calificacion;
import com.ronda.api.entity.Operacion;
import com.ronda.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByReceptor(Usuario receptor);
    List<Calificacion> findByReceptorAndRolReceptor(Usuario receptor, com.ronda.api.enums.RolCalificacion rol);
    boolean existsByOperacionAndEmisor(Operacion operacion, Usuario emisor);
}
