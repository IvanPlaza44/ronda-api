package com.ronda.api.repository;

import com.ronda.api.entity.Pregunta;
import com.ronda.api.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    List<Pregunta> findByPublicacionOrderByFechaAsc(Publicacion publicacion);
}
