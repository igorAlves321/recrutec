package recrutec.recrutec.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Ponto de entrada personalizado para tratamento de exceções de autenticação.
 * 
 * Princípios SOLID aplicados:
 * - Single Responsibility: Responsável apenas por tratar falhas de autenticação
 * - Open/Closed: Extensível para diferentes tipos de resposta de erro
 * - Dependency Inversion: Implementa a interface AuthenticationEntryPoint do Spring Security
 * 
 * Este componente é acionado quando um usuário tenta acessar um recurso protegido
 * sem estar devidamente autenticado.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Método chamado quando uma requisição não autenticada tenta acessar um recurso protegido.
     * 
     * @param request Requisição HTTP que falhou na autenticação
     * @param response Resposta HTTP onde será escrita a mensagem de erro
     * @param authException Exceção de autenticação que foi lançada
     * @throws IOException Em caso de erro de I/O ao escrever a resposta
     */
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        // Log da tentativa de acesso não autorizado
        log.warn("Acesso não autorizado detectado. URI: {}, IP: {}, User-Agent: {}", 
                request.getRequestURI(),
                getClientIpAddress(request),
                request.getHeader("User-Agent"));

        // Configura o status HTTP e tipo de conteúdo da resposta
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Cria o corpo da resposta com informações detalhadas do erro
        Map<String, Object> errorResponse = createErrorResponse(request, authException);

        // Escreve a resposta JSON no corpo da resposta HTTP
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Cria um mapa com informações detalhadas do erro de autenticação
     * 
     * @param request Requisição HTTP que falhou
     * @param authException Exceção de autenticação
     * @return Mapa com dados do erro para serialização JSON
     */
    private Map<String, Object> createErrorResponse(HttpServletRequest request, 
                                                   AuthenticationException authException) {
        Map<String, Object> errorResponse = new HashMap<>();
        
        // Informações básicas do erro
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", determineErrorMessage(authException));
        errorResponse.put("path", request.getRequestURI());
        
        // Informações adicionais para debugging (remover em produção se necessário)
        if (log.isDebugEnabled()) {
            errorResponse.put("method", request.getMethod());
            errorResponse.put("clientIp", getClientIpAddress(request));
        }

        return errorResponse;
    }

    /**
     * Determina a mensagem de erro apropriada baseada no tipo de exceção
     * 
     * @param authException Exceção de autenticação
     * @return Mensagem de erro localizada
     */
    private String determineErrorMessage(AuthenticationException authException) {
        if (authException == null) {
            return "Acesso negado. Token de autenticação necessário.";
        }

        String exceptionMessage = authException.getMessage();
        
        // Personaliza mensagens baseadas em tipos comuns de exceções
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("JWT")) {
                return "Token de autenticação inválido ou expirado.";
            } else if (exceptionMessage.contains("expired")) {
                return "Token de autenticação expirado. Faça login novamente.";
            } else if (exceptionMessage.contains("malformed")) {
                return "Token de autenticação malformado.";
            } else if (exceptionMessage.contains("signature")) {
                return "Assinatura do token inválida.";
            }
        }

        // Mensagem genérica para outros casos
        return "Acesso negado. Credenciais de autenticação inválidas ou ausentes.";
    }

    /**
     * Obtém o endereço IP real do cliente, considerando proxies e load balancers
     * 
     * @param request Requisição HTTP
     * @return Endereço IP do cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Verifica headers comuns de proxy/load balancer
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Se o header contém múltiplos IPs, pega o primeiro
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }

        // Fallback para o IP direto da conexão
        return request.getRemoteAddr();
    }
}
