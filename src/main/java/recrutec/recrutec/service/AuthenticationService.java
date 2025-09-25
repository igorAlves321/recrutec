package recrutec.recrutec.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import recrutec.recrutec.dto.AuthResponseDTO;
import recrutec.recrutec.dto.LoginRequestDTO;
import recrutec.recrutec.dto.RefreshTokenRequestDTO;
import recrutec.recrutec.exception.ResourceNotFoundException;
import recrutec.recrutec.security.jwt.JwtTokenProvider;

import java.util.Optional;

/**
 * Serviço responsável pela autenticação de usuários e gerenciamento de tokens JWT.
 * 
 * Princípios SOLID aplicados:
 * - Single Responsibility: Responsável apenas por autenticação e geração de tokens
 * - Open/Closed: Extensível para novos tipos de autenticação sem modificação
 * - Dependency Inversion: Depende de abstrações (repositories, providers)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    /**
     * Autentica um usuário e gera tokens JWT
     * 
     * @param loginRequest Dados de login do usuário
     * @return Response com tokens JWT e informações do usuário
     * @throws BadCredentialsException Se as credenciais estão incorretas
     * @throws DisabledException Se a conta do usuário está desabilitada
     */
    public AuthResponseDTO authenticate(LoginRequestDTO loginRequest) {
        log.debug("Tentando autenticar usuário: {}", loginRequest.getEmail());

        try {
            // Autentica o usuário usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getSenha()
                    )
            );

            // Gera os tokens JWT
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getEmail());

            // Obtém informações do usuário
            AuthResponseDTO.UserInfoDTO userInfo = getUserInfo(loginRequest.getEmail());

            // Calcula tempo de expiração em segundos
            long expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000;

            log.info("Usuário autenticado com sucesso: {} ({})", 
                    userInfo.getEmail(), userInfo.getRole());

            return new AuthResponseDTO(accessToken, refreshToken, expiresIn, userInfo);

        } catch (BadCredentialsException ex) {
            log.warn("Tentativa de login com credenciais inválidas para: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Email ou senha inválidos");
        } catch (DisabledException ex) {
            log.warn("Tentativa de login com conta desabilitada: {}", loginRequest.getEmail());
            throw new DisabledException("Conta de usuário desabilitada");
        } catch (Exception ex) {
            log.error("Erro durante autenticação para usuário {}: {}", loginRequest.getEmail(), ex.getMessage());
            throw new BadCredentialsException("Erro durante autenticação");
        }
    }

    /**
     * Renova o access token usando um refresh token válido
     * 
     * @param refreshRequest Requisição com refresh token
     * @return Response com novo access token
     * @throws BadCredentialsException Se o refresh token é inválido
     */
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        
        log.debug("Tentando renovar token");

        try {
            // Valida o refresh token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BadCredentialsException("Refresh token inválido");
            }

            // Verifica se é realmente um refresh token
            if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
                throw new BadCredentialsException("Token fornecido não é um refresh token");
            }

            // Extrai o username do refresh token
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

            // Carrega os detalhes do usuário
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Cria nova autenticação
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Gera novo access token
            String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
            
            // Gera novo refresh token (opcional - pode manter o mesmo)
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

            // Obtém informações do usuário
            AuthResponseDTO.UserInfoDTO userInfo = getUserInfo(username);

            long expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000;

            log.info("Token renovado com sucesso para usuário: {}", username);

            return new AuthResponseDTO(newAccessToken, newRefreshToken, expiresIn, userInfo);

        } catch (UsernameNotFoundException ex) {
            log.warn("Usuário não encontrado durante renovação de token: {}", ex.getMessage());
            throw new BadCredentialsException("Usuário não encontrado");
        } catch (Exception ex) {
            log.error("Erro durante renovação de token: {}", ex.getMessage());
            throw new BadCredentialsException("Erro durante renovação de token");
        }
    }

    /**
     * Invalida um refresh token (logout)
     * 
     * @param refreshToken Token a ser invalidado
     * @return true se invalidado com sucesso
     */
    public boolean invalidateToken(String refreshToken) {
        log.debug("Invalidando refresh token");
        
        try {
            if (jwtTokenProvider.validateToken(refreshToken)) {
                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                log.info("Token invalidado para usuário: {}", username);
                
                // Nota: Em uma implementação real, você poderia manter uma blacklist
                // de tokens invalidados em cache (Redis) ou banco de dados
                
                return true;
            }
        } catch (Exception ex) {
            log.error("Erro ao invalidar token: {}", ex.getMessage());
        }
        
        return false;
    }

    /**
     * Obtém informações básicas do usuário pelo email
     * Delegado para UserLookupService seguindo SRP
     *
     * @param email Email do usuário
     * @return Informações básicas do usuário
     * @throws UsernameNotFoundException Se o usuário não for encontrado
     */
    private AuthResponseDTO.UserInfoDTO getUserInfo(String email) {
        return userService.findByEmail(email)
                .map(user -> new AuthResponseDTO.UserInfoDTO(
                        user.getId(), user.getNome(), user.getEmail(), user.getRole().name()))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", email));
    }
}
