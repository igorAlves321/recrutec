package recrutec.recrutec.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro de autenticação JWT que intercepta todas as requisições HTTP.
 * 
 * Princípios SOLID aplicados:
 * - Single Responsibility: Responsável apenas por autenticar requisições baseadas em JWT
 * - Open/Closed: Extensível para novos tipos de autenticação sem modificar o código
 * - Liskov Substitution: Pode ser substituído por outros filtros de autenticação
 * - Interface Segregation: Usa apenas as interfaces necessárias do Spring Security
 * - Dependency Inversion: Depende da abstração JwtTokenProvider
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // Prefixo padrão para tokens Bearer
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Método principal do filtro que processa cada requisição HTTP
     * 
     * @param request Requisição HTTP
     * @param response Resposta HTTP
     * @param filterChain Cadeia de filtros
     * @throws ServletException Em caso de erro no servlet
     * @throws IOException Em caso de erro de I/O
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extrai o token JWT da requisição
            String jwt = extractTokenFromRequest(request);

            // Valida e processa o token se presente
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                authenticateUser(jwt, request);
            } else if (jwt != null) {
                log.debug("Token JWT inválido ou expirado na requisição: {}", request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("Erro ao processar autenticação JWT: {}", ex.getMessage());
            // Não propaga a exceção para não interromper o fluxo da aplicação
            // O usuário simplesmente não será autenticado
        }

        // Continue com a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do header Authorization da requisição
     * 
     * @param request Requisição HTTP
     * @return Token JWT limpo (sem prefixo Bearer) ou null se não encontrado
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Autentica o usuário baseado no token JWT válido
     * 
     * @param jwt Token JWT válido
     * @param request Requisição HTTP para detalhes de autenticação
     */
    private void authenticateUser(String jwt, HttpServletRequest request) {
        // Verifica se é um access token (não refresh token)
        if (!jwtTokenProvider.isAccessToken(jwt)) {
            log.debug("Token não é um access token válido");
            return;
        }

        // Extrai informações do usuário do token
        String username = jwtTokenProvider.getUsernameFromToken(jwt);
        String rolesString = jwtTokenProvider.getRolesFromToken(jwt);

        // Converte roles de string para lista de authorities
        List<SimpleGrantedAuthority> authorities = parseRoles(rolesString);

        // Cria UserDetails com as informações extraídas
        UserDetails userDetails = User.builder()
                .username(username)
                .password("") // Senha não é necessária para autenticação por token
                .authorities(authorities)
                .build();

        // Cria o token de autenticação do Spring Security
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                );

        // Adiciona detalhes da requisição ao contexto de autenticação
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Define a autenticação no contexto de segurança
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Usuário {} autenticado com sucesso via JWT", username);
    }

    /**
     * Converte a string de roles em uma lista de SimpleGrantedAuthority
     * 
     * @param rolesString String com roles separadas por vírgula
     * @return Lista de authorities para o Spring Security
     */
    private List<SimpleGrantedAuthority> parseRoles(String rolesString) {
        if (!StringUtils.hasText(rolesString)) {
            return List.of();
        }

        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(role -> {
                    // Adiciona prefixo ROLE_ se não estiver presente
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }
                    return new SimpleGrantedAuthority(role);
                })
                .collect(Collectors.toList());
    }

    /**
     * Determina se o filtro deve ser aplicado à requisição atual.
     * Por padrão, aplica a todas as requisições, mas pode ser sobrescrito
     * para pular certas URLs (como recursos estáticos).
     * 
     * @param request Requisição HTTP
     * @return true se o filtro deve ser aplicado, false caso contrário
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Exemplos de caminhos que podem ser ignorados pelo filtro
        // Descomente e ajuste conforme necessário
        /*
        return path.startsWith("/api/public/") || 
               path.startsWith("/swagger-") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars/") ||
               path.equals("/favicon.ico");
        */
        
        return false; // Aplica o filtro a todas as requisições por padrão
    }
}
