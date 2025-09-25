package recrutec.recrutec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import recrutec.recrutec.dto.AuthResponseDTO;
import recrutec.recrutec.dto.LoginRequestDTO;
import recrutec.recrutec.dto.RefreshTokenRequestDTO;
import recrutec.recrutec.exception.ResourceNotFoundException;
import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;
import recrutec.recrutec.security.jwt.JwtTokenProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Testes unitários para AuthenticationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService - Testes Unitários")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginRequestDTO loginRequest;
    private RefreshTokenRequestDTO refreshRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("usuario@email.com");
        loginRequest.setSenha("senha123");

        refreshRequest = new RefreshTokenRequestDTO();
        refreshRequest.setRefreshToken("refresh-token-123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setNome("João Silva");
        mockUser.setEmail("usuario@email.com");
        mockUser.setRole(Role.CANDIDATO);
    }

    @Test
    @DisplayName("Deve autenticar usuário com sucesso")
    void deveAutenticarUsuarioComSucesso() {
        // Given
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-123";
        long expiresIn = 3600L;

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtTokenProvider.generateAccessToken(authentication)).willReturn(accessToken);
        given(jwtTokenProvider.generateRefreshToken("usuario@email.com")).willReturn(refreshToken);
        given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(expiresIn * 1000);
        given(userService.findByEmail("usuario@email.com")).willReturn(Optional.of(mockUser));

        // When
        AuthResponseDTO response = authenticationService.authenticate(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
        assertThat(response.getUser().getEmail()).isEqualTo("usuario@email.com");
        assertThat(response.getUser().getRole()).isEqualTo("CANDIDATO");

        then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
        then(jwtTokenProvider).should().generateAccessToken(authentication);
        then(jwtTokenProvider).should().generateRefreshToken("usuario@email.com");
        then(userService).should().findByEmail("usuario@email.com");
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException para credenciais inválidas")
    void deveLancarBadCredentialsExceptionParaCredenciaisInvalidas() {
        // Given
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Credenciais inválidas"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Email ou senha inválidos");

        then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando usuário não existe")
    void deveLancarBadCredentialsExceptionQuandoUsuarioNaoExiste() {
        // Given
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtTokenProvider.generateAccessToken(authentication)).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken("usuario@email.com")).willReturn("refresh-token");
        given(userService.findByEmail("usuario@email.com")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Erro durante autenticação");

        then(userService).should().findByEmail("usuario@email.com");
    }

    @Test
    @DisplayName("Deve renovar token com sucesso")
    void deveRenovarTokenComSucesso() {
        // Given
        String username = "usuario@email.com";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        long expiresIn = 3600L;

        given(jwtTokenProvider.validateToken(refreshRequest.getRefreshToken())).willReturn(true);
        given(jwtTokenProvider.isRefreshToken(refreshRequest.getRefreshToken())).willReturn(true);
        given(jwtTokenProvider.getUsernameFromToken(refreshRequest.getRefreshToken())).willReturn(username);
        given(userDetailsService.loadUserByUsername(username)).willReturn(
                org.springframework.security.core.userdetails.User.builder()
                        .username(username)
                        .password("password")
                        .authorities("ROLE_CANDIDATO")
                        .build());
        given(jwtTokenProvider.generateAccessToken(any(Authentication.class))).willReturn(newAccessToken);
        given(jwtTokenProvider.generateRefreshToken(username)).willReturn(newRefreshToken);
        given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(expiresIn * 1000);
        given(userService.findByEmail(username)).willReturn(Optional.of(mockUser));

        // When
        AuthResponseDTO response = authenticationService.refreshToken(refreshRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(response.getExpiresIn()).isEqualTo(expiresIn);
        assertThat(response.getUser().getEmail()).isEqualTo(username);

        then(jwtTokenProvider).should().validateToken(refreshRequest.getRefreshToken());
        then(jwtTokenProvider).should().isRefreshToken(refreshRequest.getRefreshToken());
        then(jwtTokenProvider).should().getUsernameFromToken(refreshRequest.getRefreshToken());
        then(userDetailsService).should().loadUserByUsername(username);
        then(jwtTokenProvider).should().generateAccessToken(any(Authentication.class));
        then(jwtTokenProvider).should().generateRefreshToken(username);
        then(userService).should().findByEmail(username);
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException para refresh token inválido")
    void deveLancarBadCredentialsExceptionParaRefreshTokenInvalido() {
        // Given
        given(jwtTokenProvider.validateToken(refreshRequest.getRefreshToken())).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshToken(refreshRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Erro durante renovação de token");

        then(jwtTokenProvider).should().validateToken(refreshRequest.getRefreshToken());
    }

    @Test
    @DisplayName("Deve invalidar token com sucesso")
    void deveInvalidarTokenComSucesso() {
        // Given
        RefreshTokenRequestDTO logoutRequest = new RefreshTokenRequestDTO();
        logoutRequest.setRefreshToken("refresh-token-123");

        given(jwtTokenProvider.validateToken(logoutRequest.getRefreshToken())).willReturn(true);
        given(jwtTokenProvider.getUsernameFromToken(logoutRequest.getRefreshToken())).willReturn("usuario@email.com");

        // When
        boolean resultado = authenticationService.invalidateToken(logoutRequest.getRefreshToken());

        // Then
        assertThat(resultado).isTrue();
        then(jwtTokenProvider).should().validateToken(logoutRequest.getRefreshToken());
        then(jwtTokenProvider).should().getUsernameFromToken(logoutRequest.getRefreshToken());
    }

    @Test
    @DisplayName("Deve retornar false para invalidação com token inválido")
    void deveRetornarFalseParaInvalidacaoComTokenInvalido() {
        // Given
        RefreshTokenRequestDTO logoutRequest = new RefreshTokenRequestDTO();
        logoutRequest.setRefreshToken("invalid-token");

        given(jwtTokenProvider.validateToken(logoutRequest.getRefreshToken())).willReturn(false);

        // When
        boolean resultado = authenticationService.invalidateToken(logoutRequest.getRefreshToken());

        // Then
        assertThat(resultado).isFalse();
        then(jwtTokenProvider).should().validateToken(logoutRequest.getRefreshToken());
    }
}