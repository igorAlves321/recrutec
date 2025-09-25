package recrutec.recrutec.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Testes unitários para GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Testes Unitários")
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        given(webRequest.getDescription(false)).willReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException corretamente")
    void deveTratarResourceNotFoundExceptionCorretamente() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Usuário", "id", 1L);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleResourceNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Resource Not Found");
        assertThat(response.getBody().getMessage()).contains("Usuário não encontrado");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve tratar ResourceAlreadyExistsException corretamente")
    void deveTratarResourceAlreadyExistsExceptionCorretamente() {
        // Given
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException("Usuário", "email", "test@email.com");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleResourceAlreadyExistsException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Resource Already Exists");
        assertThat(response.getBody().getMessage()).contains("Usuário já existe");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("Deve tratar InvalidDataException corretamente")
    void deveTratarInvalidDataExceptionCorretamente() {
        // Given
        InvalidDataException exception = new InvalidDataException("Dados inválidos fornecidos");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleInvalidDataException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Invalid Data");
        assertThat(response.getBody().getMessage()).isEqualTo("Dados inválidos fornecidos");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("Deve tratar BusinessLogicException corretamente")
    void deveTratarBusinessLogicExceptionCorretamente() {
        // Given
        BusinessLogicException exception = new BusinessLogicException("Regra de negócio violada");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleBusinessLogicException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getError()).isEqualTo("Business Logic Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Regra de negócio violada");
    }

    @Test
    @DisplayName("Deve tratar UnauthorizedException corretamente")
    void deveTratarUnauthorizedExceptionCorretamente() {
        // Given
        UnauthorizedException exception = new UnauthorizedException("Acesso não autorizado");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleUnauthorizedException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo("Acesso não autorizado");
    }

    @Test
    @DisplayName("Deve tratar BadCredentialsException corretamente")
    void deveTratarBadCredentialsExceptionCorretamente() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Credenciais inválidas");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleBadCredentialsException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Authentication Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Email ou senha inválidos");
    }

    @Test
    @DisplayName("Deve tratar UsernameNotFoundException corretamente")
    void deveTratarUsernameNotFoundExceptionCorretamente() {
        // Given
        UsernameNotFoundException exception = new UsernameNotFoundException("Usuário não encontrado");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleUsernameNotFoundException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Authentication Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve tratar DisabledException corretamente")
    void deveTratarDisabledExceptionCorretamente() {
        // Given
        DisabledException exception = new DisabledException("Conta desabilitada");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDisabledException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Account Disabled");
        assertThat(response.getBody().getMessage()).isEqualTo("Conta de usuário desabilitada");
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException corretamente")
    void deveTratarAccessDeniedExceptionCorretamente() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Acesso negado");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleAccessDeniedException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Access Denied");
        assertThat(response.getBody().getMessage()).contains("Acesso negado");
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com detalhes dos campos")
    void deveTratarMethodArgumentNotValidExceptionComDetalhesDosCampos() {
        // Given
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("user", "email", "Email é obrigatório"),
                new FieldError("user", "nome", "Nome deve ter pelo menos 2 caracteres")
        );

        given(methodArgumentNotValidException.getBindingResult()).willReturn(bindingResult);
        given(bindingResult.getFieldErrors()).willReturn(fieldErrors);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleMethodArgumentNotValidException(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Os dados fornecidos não são válidos");
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails()).contains("Email é obrigatório", "Nome deve ter pelo menos 2 caracteres");
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException corretamente")
    void deveTratarHttpMessageNotReadableExceptionCorretamente() {
        // Given
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("JSON malformado");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleHttpMessageNotReadableException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Malformed JSON");
        assertThat(response.getBody().getMessage()).isEqualTo("Formato JSON inválido na requisição");
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException corretamente")
    void deveTratarIllegalArgumentExceptionCorretamente() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleIllegalArgumentException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Invalid Argument");
        assertThat(response.getBody().getMessage()).isEqualTo("Argumento inválido");
    }

    @Test
    @DisplayName("Deve tratar exceção genérica corretamente")
    void deveTratarExcecaoGenericaCorretamente() {
        // Given
        RuntimeException exception = new RuntimeException("Erro inesperado");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGlobalException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).contains("Ocorreu um erro interno no servidor");
    }
}