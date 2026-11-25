package com.ronda.api.repository;

import com.ronda.api.entity.Operacion;
import com.ronda.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperacionRepository extends JpaRepository<Operacion, Long> {
    List<Operacion> findByCompradorOrderByFechaAcordadaDesc(Usuario comprador);
    List<Operacion> findByVendedorOrderByFechaAcordadaDesc(Usuario vendedor);
}
