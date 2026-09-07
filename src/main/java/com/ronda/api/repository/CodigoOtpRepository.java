package com.ronda.api.repository;

import com.ronda.api.entity.CodigoOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoOtpRepository extends JpaRepository<CodigoOtp, Long> {
    Optional<CodigoOtp> findFirstByEmailAndUsadoFalseOrderByIdDesc(String email);
}
