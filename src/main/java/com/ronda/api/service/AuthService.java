package com.ronda.api.service;

import com.ronda.api.dto.request.LoginDto;
import com.ronda.api.dto.request.OtpVerifyDto;
import com.ronda.api.dto.response.AuthResponseDto;
import com.ronda.api.entity.Usuario;
import com.ronda.api.exception.ApiException;
import com.ronda.api.repository.UsuarioRepository;
import com.ronda.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public void solicitarOtp(String email) {
        otpService.generarYEnviar(email);
    }

    public void reenviarOtp(String email) {
        otpService.reenviar(email);
    }

    @Transactional
    public AuthResponseDto verificarOtp(OtpVerifyDto dto) {
        otpService.verificar(dto.email(), dto.codigo());

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .email(dto.email())
                        .emailVerificado(true)
                        .build()));

        if (!usuario.isEmailVerificado()) {
            usuario.setEmailVerificado(true);
            usuarioRepository.save(usuario);
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getId());
        return new AuthResponseDto(token, usuario.getId(), usuario.getEmail(), usuario.getUsername());
    }

    public AuthResponseDto login(LoginDto dto) {
        Usuario usuario = usuarioRepository.findByUsername(dto.usernameOrEmail())
                .or(() -> usuarioRepository.findByEmail(dto.usernameOrEmail()))
                .orElseThrow(() -> new ApiException("Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED));

        if (usuario.getPassword() == null || !passwordEncoder.matches(dto.password(), usuario.getPassword())) {
            throw new ApiException("Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getId());
        return new AuthResponseDto(token, usuario.getId(), usuario.getEmail(), usuario.getUsername());
    }

    @Transactional
    public void registrarUsuarioConPassword(LoginDto dto) {
        // Verificamos que no exista
        if (usuarioRepository.findByEmail(dto.usernameOrEmail()).isPresent()) {
            throw new ApiException("El usuario ya existe", HttpStatus.BAD_REQUEST);
        }
        
        Usuario nuevoUsuario = Usuario.builder()
                .email(dto.usernameOrEmail())
                .username(dto.usernameOrEmail()) // Usamos el email como username por ahora
                .password(passwordEncoder.encode(dto.password())) // Encriptamos la clave!
                .emailVerificado(true)
                .build();
                
        usuarioRepository.save(nuevoUsuario);
    }
}
