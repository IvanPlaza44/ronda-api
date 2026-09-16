package com.ronda.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void enviarOtp(String destinatario, String codigo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Tu código de verificación - Ronda");
            mensaje.setText("Tu código es: " + codigo + "\nExpira en unos minutos.");
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar el mail OTP a {}: {}", destinatario, e.getMessage());
        }
    }
}