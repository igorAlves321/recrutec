package recrutec.recrutec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recrutec.recrutec.exception.ResourceNotFoundException;
import recrutec.recrutec.model.Vaga;
import recrutec.recrutec.repository.VagaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VagaService {

    @Autowired
    private VagaRepository vagaRepository;

    // Salvar nova vaga
    public Vaga salvarVaga(Vaga vaga) {
        return vagaRepository.save(vaga);
    }

    // Listar todas as vagas
    public List<Vaga> listarVagas() {
        return vagaRepository.findAll();
    }

    // Listar apenas vagas abertas (para endpoint público)
    public List<Vaga> listarVagasAbertas() {
        return vagaRepository.findAll().stream()
                .filter(vaga -> "Aberta".equals(vaga.getStatus()))
                .collect(Collectors.toList());
    }

    // Listar vagas por email do recrutador (agora usando User)
    public List<Vaga> listarVagasPorRecrutadorEmail(String email) {
        return vagaRepository.findAll().stream()
                .filter(vaga -> vaga.getRecrutador() != null &&
                               email.equals(vaga.getRecrutador().getEmail()) &&
                               vaga.getRecrutador().isRecrutador()) // Garantir que é recrutador
                .collect(Collectors.toList());
    }

    // Buscar vaga por ID
    public Optional<Vaga> buscarVagaPorId(Long id) {
        return vagaRepository.findById(id);
    }

    // Deletar vaga por ID
    public void deletarVaga(Long id) {
        vagaRepository.deleteById(id);
    }
}
