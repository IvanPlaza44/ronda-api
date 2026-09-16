package com.ronda.api.controller;

import com.ronda.api.dto.request.LoginDto;
import com.ronda.api.dto.request.OtpRequestDto;
import com.ronda.api.dto.request.OtpVerifyDto;
import com.ronda.api.dto.response.AuthResponseDto;
import com.ronda.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Constructor explícito para que Java inicialice el final
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/solicitar")
    public ResponseEntity<Void> solicitarOtp(@Valid @RequestBody OtpRequestDto dto) {
        authService.solicitarOtp(dto.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/otp/reenviar")
    public ResponseEntity<Void> reenviarOtp(@Valid @RequestBody OtpRequestDto dto) {
        authService.reenviarOtp(dto.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/otp/verificar")
    public ResponseEntity<AuthResponseDto> verificarOtp(@Valid @RequestBody OtpVerifyDto dto) {
        return ResponseEntity.ok(authService.verificarOtp(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/registro")
    public ResponseEntity<Void> registrar(@Valid @RequestBody LoginDto dto) {
        authService.registrarUsuarioConPassword(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}