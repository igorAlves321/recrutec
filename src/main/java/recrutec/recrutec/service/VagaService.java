package recrutec.recrutec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recrutec.recrutec.model.Vaga;
import recrutec.recrutec.repository.VagaRepository;

import java.util.List;
import java.util.Optional;

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

    // Buscar vaga por ID
    public Optional<Vaga> buscarVagaPorId(Long id) {
        return vagaRepository.findById(id);
    }

    // Deletar vaga por ID
    public void deletarVaga(Long id) {
        vagaRepository.deleteById(id);
    }
}
