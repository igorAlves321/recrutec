package recrutec.recrutec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recrutec.recrutec.model.Candidato;
import recrutec.recrutec.repository.CandidatoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CandidatoService {

    @Autowired
    private CandidatoRepository candidatoRepository;

    // Salvar candidato
    public Candidato salvarCandidato(Candidato candidato) {
        return candidatoRepository.save(candidato);
    }

    // Listar todos os candidatos
    public List<Candidato> listarCandidatos() {
        return candidatoRepository.findAll();
    }

    // Buscar candidato por ID
    public Optional<Candidato> buscarCandidatoPorId(Long id) {
        return candidatoRepository.findById(id);
    }

    // Deletar candidato
    public void deletarCandidato(Long id) {
        candidatoRepository.deleteById(id);
    }
}
