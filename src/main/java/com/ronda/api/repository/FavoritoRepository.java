package com.ronda.api.repository;

import com.ronda.api.entity.Favorito;
import com.ronda.api.entity.Publicacion;
import com.ronda.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    List<Favorito> findByUsuario(Usuario usuario);
    Optional<Favorito> findByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);
    boolean existsByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);
}
