package recrutec.recrutec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import recrutec.recrutec.dto.CandidatoRegisterDTO;
import recrutec.recrutec.dto.RecrutadorRegisterDTO;
import recrutec.recrutec.exception.InvalidDataException;
import recrutec.recrutec.exception.ResourceAlreadyExistsException;
import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;
import recrutec.recrutec.repository.UserRepository;
import recrutec.recrutec.service.impl.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Testes unitários para UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Testes Unitários")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private CandidatoRegisterDTO candidatoDTO;
    private RecrutadorRegisterDTO recrutadorDTO;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setNome("João Silva");
        mockUser.setEmail("joao@email.com");
        mockUser.setSenha("senha123");
        mockUser.setRole(Role.CANDIDATO);

        candidatoDTO = new CandidatoRegisterDTO();
        candidatoDTO.setNome("Maria Santos");
        candidatoDTO.setEmail("maria@email.com");
        candidatoDTO.setSenha("senha123");
        candidatoDTO.setConfirmarSenha("senha123");
        candidatoDTO.setTelefone("(11) 99999-9999");
        candidatoDTO.setAreaInteresse(Arrays.asList("Java", "Spring"));
        candidatoDTO.setHabilidades(Arrays.asList("Backend", "API"));

        recrutadorDTO = new RecrutadorRegisterDTO();
        recrutadorDTO.setNome("Carlos Recrutador");
        recrutadorDTO.setEmail("carlos@empresa.com");
        recrutadorDTO.setSenha("senha123");
        recrutadorDTO.setConfirmarSenha("senha123");
        recrutadorDTO.setTelefone("(11) 88888-8888");
        recrutadorDTO.setEmpresa("Tech Corp");
    }

    @Test
    @DisplayName("Deve salvar usuário com senha criptografada")
    void deveSalvarUsuarioComSenhaCriptografada() {
        // Given
        String senhaCriptografada = "senhaHash123";
        given(passwordEncoder.encode(anyString())).willReturn(senhaCriptografada);
        given(userRepository.save(any(User.class))).willReturn(mockUser);

        // When
        User resultado = userService.save(mockUser);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getSenha()).isEqualTo(senhaCriptografada);
        then(passwordEncoder).should().encode("senha123");
        then(userRepository).should().save(mockUser);
    }

    @Test
    @DisplayName("Deve encontrar usuário por email")
    void deveEncontrarUsuarioPorEmail() {
        // Given
        given(userRepository.findByEmail("joao@email.com")).willReturn(Optional.of(mockUser));

        // When
        Optional<User> resultado = userService.findByEmail("joao@email.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("joao@email.com");
        then(userRepository).should().findByEmail("joao@email.com");
    }

    @Test
    @DisplayName("Deve retornar empty quando usuário não existe por email")
    void deveRetornarEmptyQuandoUsuarioNaoExistePorEmail() {
        // Given
        given(userRepository.findByEmail("inexistente@email.com")).willReturn(Optional.empty());

        // When
        Optional<User> resultado = userService.findByEmail("inexistente@email.com");

        // Then
        assertThat(resultado).isEmpty();
        then(userRepository).should().findByEmail("inexistente@email.com");
    }

    @Test
    @DisplayName("Deve listar usuários por role")
    void deveListarUsuariosPorRole() {
        // Given
        List<User> candidatos = Arrays.asList(mockUser, new User());
        given(userRepository.findByRole(Role.CANDIDATO)).willReturn(candidatos);

        // When
        List<User> resultado = userService.findByRole(Role.CANDIDATO);

        // Then
        assertThat(resultado).hasSize(2);
        then(userRepository).should().findByRole(Role.CANDIDATO);
    }

    @Test
    @DisplayName("Deve verificar se email existe")
    void deveVerificarSeEmailExiste() {
        // Given
        given(userRepository.existsByEmail("joao@email.com")).willReturn(true);

        // When
        boolean existe = userService.existsByEmail("joao@email.com");

        // Then
        assertThat(existe).isTrue();
        then(userRepository).should().existsByEmail("joao@email.com");
    }

    @Test
    @DisplayName("Deve registrar candidato com sucesso")
    void deveRegistrarCandidatoComSucesso() {
        // Given
        given(userRepository.existsByEmail("maria@email.com")).willReturn(false);
        given(passwordEncoder.encode("senha123")).willReturn("senhaHash123");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // When
        User resultado = userService.registerCandidato(candidatoDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Maria Santos");
        assertThat(resultado.getEmail()).isEqualTo("maria@email.com");
        assertThat(resultado.getRole()).isEqualTo(Role.CANDIDATO);
        assertThat(resultado.getAreaInteresse()).containsExactly("Java", "Spring");
        then(userRepository).should().existsByEmail("maria@email.com");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar candidato com email existente")
    void deveLancarExcecaoAoRegistrarCandidatoComEmailExistente() {
        // Given
        given(userRepository.existsByEmail("maria@email.com")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerCandidato(candidatoDTO))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("email");

        then(userRepository).should().existsByEmail("maria@email.com");
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar candidato com senhas diferentes")
    void deveLancarExcecaoAoRegistrarCandidatoComSenhasDiferentes() {
        // Given
        candidatoDTO.setConfirmarSenha("senhaErrada");
        given(userRepository.existsByEmail("maria@email.com")).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.registerCandidato(candidatoDTO))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("não coincidem");

        then(userRepository).should().existsByEmail("maria@email.com");
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve registrar recrutador com sucesso")
    void deveRegistrarRecrutadorComSucesso() {
        // Given
        given(userRepository.existsByEmail("carlos@empresa.com")).willReturn(false);
        given(passwordEncoder.encode("senha123")).willReturn("senhaHash123");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });

        // When
        User resultado = userService.registerRecrutador(recrutadorDTO);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Carlos Recrutador");
        assertThat(resultado.getEmail()).isEqualTo("carlos@empresa.com");
        assertThat(resultado.getRole()).isEqualTo(Role.RECRUTADOR);
        assertThat(resultado.getEmpresa()).isEqualTo("Tech Corp");
        then(userRepository).should().existsByEmail("carlos@empresa.com");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("Deve deletar usuário por ID")
    void deveDeletarUsuarioPorId() {
        // Given
        Long userId = 1L;

        // When
        userService.deleteById(userId);

        // Then
        then(userRepository).should().deleteById(userId);
    }

    @Test
    @DisplayName("Deve contar usuários por role")
    void deveContarUsuariosPorRole() {
        // Given
        given(userRepository.countByRole(Role.CANDIDATO)).willReturn(5L);

        // When
        long count = userService.countByRole(Role.CANDIDATO);

        // Then
        assertThat(count).isEqualTo(5L);
        then(userRepository).should().countByRole(Role.CANDIDATO);
    }

    @Test
    @DisplayName("Deve listar candidatos por área de interesse")
    void deveListarCandidatosPorAreaDeInteresse() {
        // Given
        List<User> candidatos = Arrays.asList(mockUser);
        given(userRepository.findCandidatosByAreaInteresse("Java")).willReturn(candidatos);

        // When
        List<User> resultado = userService.findCandidatosByAreaInteresse("Java");

        // Then
        assertThat(resultado).hasSize(1);
        then(userRepository).should().findCandidatosByAreaInteresse("Java");
    }

    @Test
    @DisplayName("Deve encontrar usuário por ID")
    void deveEncontrarUsuarioPorId() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

        // When
        Optional<User> resultado = userService.findById(1L);

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
        then(userRepository).should().findById(1L);
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosUsuarios() {
        // Given
        List<User> usuarios = Arrays.asList(mockUser, new User());
        given(userRepository.findAll()).willReturn(usuarios);

        // When
        List<User> resultado = userService.findAll();

        // Then
        assertThat(resultado).hasSize(2);
        then(userRepository).should().findAll();
    }
}