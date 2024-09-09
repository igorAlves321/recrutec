package recrutec.recrutec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recrutec.recrutec.model.Recrutador;
import recrutec.recrutec.repository.RecrutadorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RecrutadorService {

    @Autowired
    private RecrutadorRepository recrutadorRepository;

    // Salvar recrutador
    public Recrutador salvarRecrutador(Recrutador recrutador) {
        return recrutadorRepository.save(recrutador);
    }

    // Listar todos os recrutadores
    public List<Recrutador> listarRecrutadores() {
        return recrutadorRepository.findAll();
    }

    // Buscar recrutador por ID
    public Optional<Recrutador> buscarRecrutadorPorId(Long id) {
        return recrutadorRepository.findById(id);
    }

    // Deletar recrutador
    public void deletarRecrutador(Long id) {
        recrutadorRepository.deleteById(id);
    }
}
