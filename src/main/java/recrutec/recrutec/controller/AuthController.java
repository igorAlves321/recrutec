package recrutec.recrutec.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.dto.*;
import recrutec.recrutec.exception.ErrorResponse;
import recrutec.recrutec.model.User;
import recrutec.recrutec.service.AuthenticationService;
import recrutec.recrutec.service.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador responsável pelos endpoints de autenticação.
 * 
 * Princípios SOLID aplicados:
 * - Single Responsibility: Responsável apenas por endpoints de autenticação
 * - Open/Closed: Extensível para novos endpoints sem modificar código existente
 * - Dependency Inversion: Depende da abstração AuthenticationService
 * 
 * Endpoints disponíveis:
 * - POST /api/auth/login: Autenticar usuário
 * - POST /api/auth/refresh: Renovar access token
 * - POST /api/auth/logout: Fazer logout do usuário
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
@Tag(name = "Autenticação", description = "Endpoints para autenticação, registro e gerenciamento de tokens JWT")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Endpoint para autenticação de usuários
     *
     * @param loginRequest Dados de login (email e senha)
     * @return Response com tokens JWT e dados do usuário
     */
    @Operation(summary = "Autenticar usuário",
               description = "Autentica um usuário e retorna tokens JWT para acesso aos endpoints protegidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.debug("Requisição de login recebida para: {}", loginRequest.getEmail());

        try {
            AuthResponseDTO authResponse = authenticationService.authenticate(loginRequest);
            
            log.info("Login bem-sucedido para usuário: {}", loginRequest.getEmail());
            
            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException ex) {
            log.warn("Tentativa de login com credenciais inválidas: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Credenciais inválidas", "Email ou senha incorretos"));

        } catch (DisabledException ex) {
            log.warn("Tentativa de login com conta desabilitada: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Conta desabilitada", "Sua conta foi desabilitada. Contate o suporte."));

        } catch (Exception ex) {
            log.error("Erro inesperado durante login para {}: {}", loginRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro interno", "Erro interno do servidor. Tente novamente mais tarde."));
        }
    }

    /**
     * Endpoint para renovação de access token
     * 
     * @param refreshRequest Dados com refresh token
     * @return Response com novos tokens JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        log.debug("Requisição de renovação de token recebida");

        try {
            AuthResponseDTO authResponse = authenticationService.refreshToken(refreshRequest);
            
            log.debug("Token renovado com sucesso");
            
            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException ex) {
            log.warn("Tentativa de renovação com token inválido: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Token inválido", "Refresh token inválido ou expirado"));

        } catch (Exception ex) {
            log.error("Erro durante renovação de token: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro interno", "Erro interno do servidor. Tente novamente mais tarde."));
        }
    }

    /**
     * Endpoint para logout do usuário
     * 
     * @param refreshRequest Dados com refresh token para invalidação
     * @return Response de confirmação de logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        log.debug("Requisição de logout recebida");

        try {
            boolean invalidated = authenticationService.invalidateToken(refreshRequest.getRefreshToken());
            
            if (invalidated) {
                log.debug("Logout realizado com sucesso");
                return ResponseEntity.ok(createSuccessResponse("Logout realizado com sucesso"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Erro no logout", "Não foi possível invalidar o token"));
            }

        } catch (Exception ex) {
            log.error("Erro durante logout: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro interno", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para verificar se o usuário está autenticado
     * Este endpoint pode ser usado pelo frontend para verificar se o token ainda é válido
     * 
     * @return Response confirmando que o usuário está autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // Este endpoint será protegido pelo filtro JWT
        // Se chegou até aqui, significa que o token é válido
        
        log.debug("Verificação de autenticação realizada");
        
        return ResponseEntity.ok(createSuccessResponse("Usuário autenticado"));
    }

    /**
     * Endpoint para registro de candidatos
     *
     * @param registerRequest Dados de registro do candidato
     * @return Response com dados do usuário criado
     */
    @Operation(summary = "Registrar candidato",
               description = "Registra um novo candidato no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Candidato registrado com sucesso",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRegisterResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register/candidato")
    public ResponseEntity<?> registerCandidato(@Valid @RequestBody CandidatoRegisterDTO registerRequest) {
        log.debug("Requisição de registro de candidato recebida para: {}", registerRequest.getEmail());

        try {
            User candidato = userService.registerCandidato(registerRequest);
            UserRegisterResponseDTO response = new UserRegisterResponseDTO(
                    candidato.getId(),
                    candidato.getNome(),
                    candidato.getEmail(),
                    candidato.getRole().name()
            );

            log.info("Candidato registrado com sucesso: {}", registerRequest.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            log.warn("Erro de validação no registro de candidato para {}: {}",
                    registerRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erro de validação", ex.getMessage()));

        } catch (Exception ex) {
            log.error("Erro inesperado durante registro de candidato para {}: {}",
                    registerRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro interno", "Erro interno do servidor. Tente novamente mais tarde."));
        }
    }

    /**
     * Endpoint para registro de recrutadores
     *
     * @param registerRequest Dados de registro do recrutador
     * @return Response com dados do usuário criado
     */
    @PostMapping("/register/recrutador")
    public ResponseEntity<?> registerRecrutador(@Valid @RequestBody RecrutadorRegisterDTO registerRequest) {
        log.debug("Requisição de registro de recrutador recebida para: {}", registerRequest.getEmail());

        try {
            User recrutador = userService.registerRecrutador(registerRequest);
            UserRegisterResponseDTO response = new UserRegisterResponseDTO(
                    recrutador.getId(),
                    recrutador.getNome(),
                    recrutador.getEmail(),
                    recrutador.getRole().name()
            );

            log.info("Recrutador registrado com sucesso: {}", registerRequest.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            log.warn("Erro de validação no registro de recrutador para {}: {}",
                    registerRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Erro de validação", ex.getMessage()));

        } catch (Exception ex) {
            log.error("Erro inesperado durante registro de recrutador para {}: {}",
                    registerRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro interno", "Erro interno do servidor. Tente novamente mais tarde."));
        }
    }

    /**
     * Endpoint de health check para o serviço de autenticação
     *
     * @return Status do serviço
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Authentication Service",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Cria uma resposta de erro padronizada
     * 
     * @param error Tipo do erro
     * @param message Mensagem do erro
     * @return Map com estrutura de erro
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Cria uma resposta de sucesso padronizada
     * 
     * @param message Mensagem de sucesso
     * @return Map com estrutura de sucesso
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
