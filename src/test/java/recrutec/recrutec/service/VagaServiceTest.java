package recrutec.recrutec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;
import recrutec.recrutec.model.Vaga;
import recrutec.recrutec.repository.VagaRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Testes unitários para VagaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VagaService - Testes Unitários")
class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @InjectMocks
    private VagaService vagaService;

    private Vaga mockVaga;
    private User mockRecrutador;
    private User mockCandidato;

    @BeforeEach
    void setUp() {
        mockRecrutador = new User();
        mockRecrutador.setId(1L);
        mockRecrutador.setNome("Recrutador Silva");
        mockRecrutador.setEmail("recrutador@empresa.com");
        mockRecrutador.setRole(Role.RECRUTADOR);
        mockRecrutador.setEmpresa("Tech Corp");

        mockCandidato = new User();
        mockCandidato.setId(2L);
        mockCandidato.setNome("João Candidato");
        mockCandidato.setEmail("joao@email.com");
        mockCandidato.setRole(Role.CANDIDATO);

        mockVaga = new Vaga();
        mockVaga.setId(1L);
        mockVaga.setTitulo("Desenvolvedor Java");
        mockVaga.setDescricao("Vaga para desenvolvedor Java senior");
        mockVaga.setStatus("Aberta");
        mockVaga.setRecrutador(mockRecrutador);
        mockVaga.setDataPostagem(LocalDate.now());
    }

    @Test
    @DisplayName("Deve salvar vaga com sucesso")
    void deveSalvarVagaComSucesso() {
        // Given
        given(vagaRepository.save(any(Vaga.class))).willReturn(mockVaga);

        // When
        Vaga resultado = vagaService.salvarVaga(mockVaga);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Desenvolvedor Java");
        assertThat(resultado.getStatus()).isEqualTo("Aberta");
        then(vagaRepository).should().save(mockVaga);
    }

    @Test
    @DisplayName("Deve listar todas as vagas")
    void deveListarTodasVagas() {
        // Given
        List<Vaga> vagas = Arrays.asList(mockVaga, new Vaga());
        given(vagaRepository.findAll()).willReturn(vagas);

        // When
        List<Vaga> resultado = vagaService.listarVagas();

        // Then
        assertThat(resultado).hasSize(2);
        then(vagaRepository).should().findAll();
    }

    @Test
    @DisplayName("Deve listar apenas vagas abertas")
    void deveListarApenasVagasAbertas() {
        // Given
        Vaga vagaFechada = new Vaga();
        vagaFechada.setStatus("Fechada");

        List<Vaga> todasVagas = Arrays.asList(mockVaga, vagaFechada);
        given(vagaRepository.findAll()).willReturn(todasVagas);

        // When
        List<Vaga> resultado = vagaService.listarVagasAbertas();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getStatus()).isEqualTo("Aberta");
        then(vagaRepository).should().findAll();
    }

    @Test
    @DisplayName("Deve listar vagas por email do recrutador")
    void deveListarVagasPorEmailRecrutador() {
        // Given
        List<Vaga> todasVagas = Arrays.asList(mockVaga);
        given(vagaRepository.findAll()).willReturn(todasVagas);

        // When
        List<Vaga> resultado = vagaService.listarVagasPorRecrutadorEmail("recrutador@empresa.com");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRecrutador().getEmail()).isEqualTo("recrutador@empresa.com");
        then(vagaRepository).should().findAll();
    }

    @Test
    @DisplayName("Deve filtrar vagas de recrutador específico corretamente")
    void deveFiltrarVagasDeRecrutadorEspecificoCorretamente() {
        // Given
        User outroRecrutador = new User();
        outroRecrutador.setEmail("outro@empresa.com");
        outroRecrutador.setRole(Role.RECRUTADOR);

        Vaga vagaOutroRecrutador = new Vaga();
        vagaOutroRecrutador.setRecrutador(outroRecrutador);

        List<Vaga> todasVagas = Arrays.asList(mockVaga, vagaOutroRecrutador);
        given(vagaRepository.findAll()).willReturn(todasVagas);

        // When
        List<Vaga> resultado = vagaService.listarVagasPorRecrutadorEmail("recrutador@empresa.com");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRecrutador().getEmail()).isEqualTo("recrutador@empresa.com");
        then(vagaRepository).should().findAll();
    }

    @Test
    @DisplayName("Deve buscar vaga por ID")
    void deveBuscarVagaPorId() {
        // Given
        given(vagaRepository.findById(1L)).willReturn(Optional.of(mockVaga));

        // When
        Optional<Vaga> resultado = vagaService.buscarVagaPorId(1L);

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
        assertThat(resultado.get().getTitulo()).isEqualTo("Desenvolvedor Java");
        then(vagaRepository).should().findById(1L);
    }

    @Test
    @DisplayName("Deve retornar empty quando vaga não existe")
    void deveRetornarEmptyQuandoVagaNaoExiste() {
        // Given
        given(vagaRepository.findById(999L)).willReturn(Optional.empty());

        // When
        Optional<Vaga> resultado = vagaService.buscarVagaPorId(999L);

        // Then
        assertThat(resultado).isEmpty();
        then(vagaRepository).should().findById(999L);
    }

    @Test
    @DisplayName("Deve deletar vaga por ID")
    void deveDeletarVagaPorId() {
        // Given
        Long vagaId = 1L;

        // When
        vagaService.deletarVaga(vagaId);

        // Then
        then(vagaRepository).should().deleteById(vagaId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há vagas do recrutador")
    void deveRetornarListaVaziaQuandoNaoHaVagasDoRecrutador() {
        // Given
        given(vagaRepository.findAll()).willReturn(Arrays.asList());

        // When
        List<Vaga> resultado = vagaService.listarVagasPorRecrutadorEmail("inexistente@empresa.com");

        // Then
        assertThat(resultado).isEmpty();
        then(vagaRepository).should().findAll();
    }

    @Test
    @DisplayName("Deve ignorar vagas com recrutador nulo")
    void deveIgnorarVagasComRecrutadorNulo() {
        // Given
        Vaga vagaSemRecrutador = new Vaga();
        vagaSemRecrutador.setRecrutador(null);

        List<Vaga> todasVagas = Arrays.asList(mockVaga, vagaSemRecrutador);
        given(vagaRepository.findAll()).willReturn(todasVagas);

        // When
        List<Vaga> resultado = vagaService.listarVagasPorRecrutadorEmail("recrutador@empresa.com");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRecrutador()).isNotNull();
        then(vagaRepository).should().findAll();
    }

    @Test
    @DisplayName("Deve filtrar apenas recrutadores válidos")
    void deveFiltrarApenasRecrutadoresValidos() {
        // Given
        User usuarioAdmin = new User();
        usuarioAdmin.setEmail("admin@empresa.com");
        usuarioAdmin.setRole(Role.ADMIN);

        Vaga vagaDeAdmin = new Vaga();
        vagaDeAdmin.setRecrutador(usuarioAdmin);

        List<Vaga> todasVagas = Arrays.asList(mockVaga, vagaDeAdmin);
        given(vagaRepository.findAll()).willReturn(todasVagas);

        // When
        List<Vaga> resultado = vagaService.listarVagasPorRecrutadorEmail("admin@empresa.com");

        // Then
        assertThat(resultado).isEmpty(); // Admin não é recrutador
        then(vagaRepository).should().findAll();
    }
}