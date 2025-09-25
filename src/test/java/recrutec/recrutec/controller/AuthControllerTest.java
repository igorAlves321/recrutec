package recrutec.recrutec.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import recrutec.recrutec.dto.*;
import recrutec.recrutec.exception.ResourceAlreadyExistsException;
import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;
import recrutec.recrutec.service.AuthenticationService;
import recrutec.recrutec.service.UserService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para AuthController
 */
@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@DisplayName("AuthController - Testes Unitários")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;


    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO loginRequest;
    private CandidatoRegisterDTO candidatoRegisterDTO;
    private RecrutadorRegisterDTO recrutadorRegisterDTO;
    private AuthResponseDTO authResponse;
    private User mockUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("usuario@email.com");
        loginRequest.setSenha("senha123");

        candidatoRegisterDTO = new CandidatoRegisterDTO();
        candidatoRegisterDTO.setNome("João Silva");
        candidatoRegisterDTO.setEmail("joao@email.com");
        candidatoRegisterDTO.setSenha("senha123");
        candidatoRegisterDTO.setConfirmarSenha("senha123");
        candidatoRegisterDTO.setTelefone("(11) 99999-9999");
        candidatoRegisterDTO.setAreaInteresse(Arrays.asList("Java", "Spring"));

        recrutadorRegisterDTO = new RecrutadorRegisterDTO();
        recrutadorRegisterDTO.setNome("Maria Recrutadora");
        recrutadorRegisterDTO.setEmail("maria@empresa.com");
        recrutadorRegisterDTO.setSenha("senha123");
        recrutadorRegisterDTO.setConfirmarSenha("senha123");
        recrutadorRegisterDTO.setTelefone("(11) 88888-8888");
        recrutadorRegisterDTO.setEmpresa("Tech Corp");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setNome("João Silva");
        mockUser.setEmail("joao@email.com");
        mockUser.setRole(Role.CANDIDATO);

        AuthResponseDTO.UserInfoDTO userInfo = new AuthResponseDTO.UserInfoDTO(
                1L, "João Silva", "joao@email.com", "CANDIDATO"
        );

        authResponse = new AuthResponseDTO(
                "access-token-123",
                "refresh-token-123",
                3600L,
                userInfo
        );
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() throws Exception {
        // Given
        given(authenticationService.authenticate(any(LoginRequestDTO.class)))
                .willReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.email").value("joao@email.com"))
                .andExpect(jsonPath("$.user.role").value("CANDIDATO"));
    }

    @Test
    @DisplayName("Deve retornar 401 para credenciais inválidas")
    void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        // Given
        given(authenticationService.authenticate(any(LoginRequestDTO.class)))
                .willThrow(new BadCredentialsException("Credenciais inválidas"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Authentication Failed"))
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }

    @Test
    @DisplayName("Deve registrar candidato com sucesso")
    void deveRegistrarCandidatoComSucesso() throws Exception {
        // Given
        given(userService.registerCandidato(any(CandidatoRegisterDTO.class)))
                .willReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/api/auth/register/candidato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidatoRegisterDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.role").value("CANDIDATO"));
    }

    @Test
    @DisplayName("Deve retornar 409 ao registrar candidato com email existente")
    void deveRetornar409AoRegistrarCandidatoComEmailExistente() throws Exception {
        // Given
        given(userService.registerCandidato(any(CandidatoRegisterDTO.class)))
                .willThrow(new ResourceAlreadyExistsException("Usuário", "email", "joao@email.com"));

        // When & Then
        mockMvc.perform(post("/api/auth/register/candidato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidatoRegisterDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Resource Already Exists"));
    }

    @Test
    @DisplayName("Deve retornar 400 para dados inválidos no registro de candidato")
    void deveRetornar400ParaDadosInvalidosNoRegistroDeCandidato() throws Exception {
        // Given
        candidatoRegisterDTO.setEmail("email-invalido"); // Email inválido

        // When & Then
        mockMvc.perform(post("/api/auth/register/candidato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidatoRegisterDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve registrar recrutador com sucesso")
    void deveRegistrarRecrutadorComSucesso() throws Exception {
        // Given
        User mockRecrutador = new User();
        mockRecrutador.setId(2L);
        mockRecrutador.setNome("Maria Recrutadora");
        mockRecrutador.setEmail("maria@empresa.com");
        mockRecrutador.setRole(Role.RECRUTADOR);

        given(userService.registerRecrutador(any(RecrutadorRegisterDTO.class)))
                .willReturn(mockRecrutador);

        // When & Then
        mockMvc.perform(post("/api/auth/register/recrutador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recrutadorRegisterDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Maria Recrutadora"))
                .andExpect(jsonPath("$.email").value("maria@empresa.com"))
                .andExpect(jsonPath("$.role").value("RECRUTADOR"));
    }

    @Test
    @DisplayName("Deve retornar 400 para JSON malformado")
    void deveRetornar400ParaJsonMalformado() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed JSON"));
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios no login")
    void deveValidarCamposObrigatoriosNoLogin() throws Exception {
        // Given
        LoginRequestDTO loginInvalido = new LoginRequestDTO();
        // Email e senha em branco

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar email no formato correto")
    void deveValidarEmailNoFormatoCorreto() throws Exception {
        // Given
        candidatoRegisterDTO.setEmail("email-sem-arroba");

        // When & Then
        mockMvc.perform(post("/api/auth/register/candidato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candidatoRegisterDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 409 ao registrar recrutador com email existente")
    void deveRetornar409AoRegistrarRecrutadorComEmailExistente() throws Exception {
        // Given
        willThrow(new ResourceAlreadyExistsException("Usuário", "email", "maria@empresa.com"))
                .given(userService).registerRecrutador(any(RecrutadorRegisterDTO.class));

        // When & Then
        mockMvc.perform(post("/api/auth/register/recrutador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recrutadorRegisterDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Resource Already Exists"));
    }
}