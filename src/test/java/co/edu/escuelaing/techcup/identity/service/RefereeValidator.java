package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class RefereeValidator {

    private final UserRepository userRepository;

    public RefereeValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validate(RefereeRequestDTO dto) {
        validateGmailDomain(dto.email());
        validateEmailUnique(dto.email());
        validateIdNumberUnique(dto.idNumber());
    }

    private void validateGmailDomain(String email) {
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            throw new BusinessException("Los árbitros deben usar una cuenta personal de Gmail.");
        }
    }

    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Este correo ya está registrado.");
        }
    }

    private void validateIdNumberUnique(String idNumber) {
        if (userRepository.existsByIdNumber(idNumber)) {
            throw new BusinessException("Este número de identificación ya existe.");
        }
    }
}