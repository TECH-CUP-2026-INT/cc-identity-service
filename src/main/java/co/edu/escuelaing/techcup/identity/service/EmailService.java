package co.edu.escuelaing.techcup.identity.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendRefereeCredentials(String email, String password) {
        // STUB: Esta lógica la completará quien se encargue de Spring Mail
        System.out.println("LOG: Enviando credenciales de árbitro a " + email);
    }
}