package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.TokenValidationUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenValidationUseCaseImpl implements TokenValidationUseCase {

    private final JwtUtil jwtUtil;
    private final UserRepositoryPort userRepository;
    private final RevokedTokenRepositoryPort revokedTokenRepository;

    @Override
    public User validateToken(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new InvalidTokenException("Token is invalid or expired");
        }

        if (revokedTokenRepository.existsByToken(token)) {
            throw new InvalidTokenException("Token has been revoked");
        }

        String userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
