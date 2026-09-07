package com.ronda.api.service;

import com.ronda.api.entity.Publicacion;
import com.ronda.api.enums.EstadoArticulo;
import com.ronda.api.enums.EstadoPublicacion;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class PublicacionSpecifications {

    private PublicacionSpecifications() {}

    public static Specification<Publicacion> estado(EstadoPublicacion estado) {
        return (root, query, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado);
    }

    public static Specification<Publicacion> texto(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) return null;
            String like = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("titulo")), like),
                    cb.like(cb.lower(root.get("descripcion")), like)
            );
        };
    }

    public static Specification<Publicacion> categoria(Long categoriaId) {
        return (root, query, cb) -> categoriaId == null ? null : cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Publicacion> precioMinimo(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("precio"), min);
    }

    public static Specification<Publicacion> precioMaximo(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("precio"), max);
    }

    public static Specification<Publicacion> estadoArticulo(EstadoArticulo estadoArticulo) {
        return (root, query, cb) -> estadoArticulo == null ? null : cb.equal(root.get("estadoArticulo"), estadoArticulo);
    }

    public static Specification<Publicacion> zona(String zona) {
        return (root, query, cb) -> (zona == null || zona.isBlank()) ? null : cb.equal(root.get("zonaEntrega"), zona);
    }
}
