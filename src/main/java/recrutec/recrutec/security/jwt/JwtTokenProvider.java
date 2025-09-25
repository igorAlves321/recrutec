package recrutec.recrutec.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Componente responsável pela geração, validação e extração de informações dos tokens JWT.
 * 
 * Princípios SOLID aplicados:
 * - Single Responsibility: Responsável apenas pela manipulação de tokens JWT
 * - Open/Closed: Extensível para novos tipos de claims sem modificar código existente
 * - Dependency Inversion: Depende de abstrações (Spring Security) não de implementações concretas
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    /**
     * Construtor que inicializa as configurações JWT a partir do application.properties
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Gera um token JWT baseado na autenticação do usuário
     * 
     * @param authentication Objeto de autenticação contendo dados do usuário
     * @return Token JWT assinado
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration);

        // Extrai as roles/authorities do usuário para incluir no token
        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Gera um refresh token para renovação de acesso
     * 
     * @param username Nome do usuário
     * @return Refresh token JWT
     */
    public String generateRefreshToken(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrai o username (subject) do token JWT
     * 
     * @param token Token JWT
     * @return Username extraído do token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extrai as roles do usuário do token JWT
     * 
     * @param token Token JWT
     * @return String com roles separadas por vírgula
     */
    public String getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roles", String.class);
    }

    /**
     * Verifica se o token é um access token
     * 
     * @param token Token JWT
     * @return true se for access token, false caso contrário
     */
    public boolean isAccessToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return "ACCESS".equals(claims.get("type", String.class));
    }

    /**
     * Verifica se o token é um refresh token
     * 
     * @param token Token JWT
     * @return true se for refresh token, false caso contrário
     */
    public boolean isRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return "REFRESH".equals(claims.get("type", String.class));
    }

    /**
     * Valida se o token JWT é válido (assinatura e expiração)
     * 
     * @param token Token JWT a ser validado
     * @return true se o token for válido, false caso contrário
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Token JWT com assinatura inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Token JWT malformado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Token JWT não suportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("Token JWT vazio: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Verifica se o token está expirado
     * 
     * @param token Token JWT
     * @return true se expirado, false caso contrário
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Extrai todas as claims (reivindicações) do token
     * 
     * @param token Token JWT
     * @return Claims extraídas do token
     * @throws JwtException se o token for inválido
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtém o tempo de expiração do access token em milissegundos
     * 
     * @return Tempo de expiração do access token
     */
    public long getAccessTokenExpiration() {
        return jwtExpiration;
    }

    /**
     * Obtém o tempo de expiração do refresh token em milissegundos
     * 
     * @return Tempo de expiração do refresh token
     */
    public long getRefreshTokenExpiration() {
        return refreshExpiration;
    }
}
