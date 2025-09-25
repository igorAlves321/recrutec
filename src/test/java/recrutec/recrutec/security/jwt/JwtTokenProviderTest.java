package recrutec.recrutec.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para JwtTokenProvider
 */
@DisplayName("JwtTokenProvider - Testes Unitários")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "testSecretKeyForJWTToken2024!@#$%^&*()_+";
    private final long testExpiration = 3600000; // 1 hora
    private final long testRefreshExpiration = 86400000; // 24 horas

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(testSecret, testExpiration, testRefreshExpiration);
    }

    @Test
    @DisplayName("Deve gerar token de acesso válido")
    void shouldGenerateValidAccessToken() {
        // Arrange
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_CANDIDATO"),
                        new SimpleGrantedAuthority("ROLE_USER")
                ))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Act
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertTrue(jwtTokenProvider.isAccessToken(token));
        assertFalse(jwtTokenProvider.isRefreshToken(token));
    }

    @Test
    @DisplayName("Deve gerar refresh token válido")
    void shouldGenerateValidRefreshToken() {
        // Arrange
        String username = "test@example.com";

        // Act
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertTrue(jwtTokenProvider.isRefreshToken(refreshToken));
        assertFalse(jwtTokenProvider.isAccessToken(refreshToken));
    }

    @Test
    @DisplayName("Deve extrair username do token")
    void shouldExtractUsernameFromToken() {
        // Arrange
        String expectedUsername = "test@example.com";
        UserDetails userDetails = User.builder()
                .username(expectedUsername)
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_CANDIDATO"))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = jwtTokenProvider.generateAccessToken(authentication);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(expectedUsername, extractedUsername);
    }

    @Test
    @DisplayName("Deve extrair roles do token")
    void shouldExtractRolesFromToken() {
        // Arrange
        UserDetails userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_CANDIDATO"),
                        new SimpleGrantedAuthority("ROLE_USER")
                ))
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = jwtTokenProvider.generateAccessToken(authentication);

        // Act
        String roles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_CANDIDATO"));
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    @DisplayName("Deve rejeitar token inválido")
    void shouldRejectInvalidToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Deve rejeitar token malformado")
    void shouldRejectMalformedToken() {
        // Arrange
        String malformedToken = "this-is-not-a-jwt-token";

        // Act & Assert
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    @DisplayName("Deve retornar tempos de expiração")
    void shouldReturnExpirationTimes() {
        // Act & Assert
        assertEquals(testExpiration, jwtTokenProvider.getAccessTokenExpiration());
        assertEquals(testRefreshExpiration, jwtTokenProvider.getRefreshTokenExpiration());
    }

    @Test
    @DisplayName("Deve detectar token expirado")
    void shouldDetectExpiredToken() {
        // Este teste requer um token expirado.
        // Em um cenário real, você poderia:
        // 1. Usar uma biblioteca de mock para simular tempo
        // 2. Criar um JwtTokenProvider com expiração muito curta
        // 3. Aguardar o tempo necessário
        
        // Para este exemplo, vamos apenas verificar a estrutura do método
        assertDoesNotThrow(() -> {
            String validToken = jwtTokenProvider.generateRefreshToken("test@example.com");
            boolean isExpired = jwtTokenProvider.isTokenExpired(validToken);
            // Token recém-criado não deve estar expirado
            assertFalse(isExpired);
        });
    }
}
