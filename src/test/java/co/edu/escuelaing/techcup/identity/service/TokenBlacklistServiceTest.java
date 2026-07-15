package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.RevokedTokenDocument;
import co.edu.escuelaing.techcup.identity.repository.RevokedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    private JwtService jwtService;
    private TokenBlacklistService tokenBlacklistService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "test-secret-key-that-is-at-least-256-bits-long-for-testing");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", 604800000L);

        tokenBlacklistService = new TokenBlacklistService(revokedTokenRepository, jwtService);

        userDetails = new User("user@test.com", "hashedPassword", Collections.emptyList());
    }

    @Test
    void revoke_validToken_savesHashedRecord() {
        String token = jwtService.generateAccessToken(userDetails);
        when(revokedTokenRepository.existsByTokenHash(anyString())).thenReturn(false);

        tokenBlacklistService.revoke(token);

        ArgumentCaptor<RevokedTokenDocument> captor = ArgumentCaptor.forClass(RevokedTokenDocument.class);
        verify(revokedTokenRepository).save(captor.capture());

        RevokedTokenDocument saved = captor.getValue();
        assertNotNull(saved.getTokenHash());
        assertFalse(saved.getTokenHash().contains(token));
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void revoke_alreadyRevoked_doesNotSaveDuplicate() {
        String token = jwtService.generateAccessToken(userDetails);
        when(revokedTokenRepository.existsByTokenHash(anyString())).thenReturn(true);

        tokenBlacklistService.revoke(token);

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void revoke_expiredToken_doesNothing() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String token = jwtService.generateAccessToken(userDetails);

        tokenBlacklistService.revoke(token);

        verify(revokedTokenRepository, never()).existsByTokenHash(anyString());
        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void isRevoked_tokenNotRevoked_returnsFalse() {
        String token = jwtService.generateAccessToken(userDetails);
        when(revokedTokenRepository.existsByTokenHash(anyString())).thenReturn(false);

        assertFalse(tokenBlacklistService.isRevoked(token));
    }

    @Test
    void isRevoked_tokenRevoked_returnsTrue() {
        String token = jwtService.generateAccessToken(userDetails);
        when(revokedTokenRepository.existsByTokenHash(anyString())).thenReturn(true);

        assertTrue(tokenBlacklistService.isRevoked(token));
    }
}
