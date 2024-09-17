package recrutec.recrutec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.model.Vaga;
import recrutec.recrutec.model.Candidato;
import recrutec.recrutec.model.Recrutador;
import recrutec.recrutec.service.VagaService;
import recrutec.recrutec.service.CandidatoService;
import recrutec.recrutec.service.RecrutadorService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vagas")
public class VagaController {

    @Autowired
    private VagaService vagaService;

    @Autowired
    private CandidatoService candidatoService;

    @Autowired
    private RecrutadorService recrutadorService;

    // Endpoint para listar todas as vagas
    @GetMapping
    public ResponseEntity<List<Vaga>> listarVagas() {
        List<Vaga> vagas = vagaService.listarVagas();
        return new ResponseEntity<>(vagas, HttpStatus.OK);
    }

    // Endpoint para buscar uma vaga por ID
    @GetMapping("/{id}")
    public ResponseEntity<Vaga> buscarVagaPorId(@PathVariable Long id) {
        Optional<Vaga> vaga = vagaService.buscarVagaPorId(id);
        return vaga.map(v -> new ResponseEntity<>(v, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Endpoint para salvar uma nova vaga, associando ao recrutador
    @PostMapping
    public ResponseEntity<Vaga> salvarVaga(@RequestBody Vaga vaga, @RequestParam Long recrutadorId) {
        Optional<Recrutador> recrutador = recrutadorService.buscarRecrutadorPorId(recrutadorId);

        if (recrutador.isPresent()) {
            vaga.setRecrutador(recrutador.get());  // Associa o recrutador à vaga
            Vaga novaVaga = vagaService.salvarVaga(vaga);
            return new ResponseEntity<>(novaVaga, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Recrutador não encontrado
    }

    // Endpoint para atualizar uma vaga existente (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Vaga> atualizarVaga(@PathVariable Long id, @RequestBody Vaga vagaAtualizada, @RequestParam Long recrutadorId) {
        Optional<Vaga> vagaOptional = vagaService.buscarVagaPorId(id);
        Optional<Recrutador> recrutadorOptional = recrutadorService.buscarRecrutadorPorId(recrutadorId);

        if (vagaOptional.isPresent() && recrutadorOptional.isPresent()) {
            Vaga vagaExistente = vagaOptional.get();
            vagaExistente.setTitulo(vagaAtualizada.getTitulo());
            vagaExistente.setDescricao(vagaAtualizada.getDescricao());
            vagaExistente.setRecrutador(recrutadorOptional.get());  // Atualiza o recrutador associado, se necessário
            vagaExistente.setStatus(vagaAtualizada.getStatus());  // Permite atualizar o status
            Vaga vagaAtualizadaSalva = vagaService.salvarVaga(vagaExistente);
            return new ResponseEntity<>(vagaAtualizadaSalva, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para deletar uma vaga por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVaga(@PathVariable Long id) {
        Optional<Vaga> vaga = vagaService.buscarVagaPorId(id);
        if (vaga.isPresent()) {
            vagaService.deletarVaga(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para inscrever um candidato em uma vaga
    @PostMapping("/{vagaId}/inscrever")
    public ResponseEntity<String> inscreverCandidato(@PathVariable Long vagaId, @RequestParam Long candidatoId) {
        Optional<Vaga> vagaOptional = vagaService.buscarVagaPorId(vagaId);
        Optional<Candidato> candidatoOptional = candidatoService.buscarCandidatoPorId(candidatoId);

        if (vagaOptional.isPresent() && candidatoOptional.isPresent()) {
            Vaga vaga = vagaOptional.get();
            Candidato candidato = candidatoOptional.get();

            // Verifica se a vaga está aberta para novas inscrições
            if (!"Aberta".equals(vaga.getStatus())) {
                return new ResponseEntity<>("A vaga não está aberta para inscrições.", HttpStatus.BAD_REQUEST);
            }

            // Verificar se o candidato já está inscrito na vaga
            if (vaga.getCandidatosInscritos().contains(candidato)) {
                return new ResponseEntity<>("Candidato já inscrito nesta vaga.", HttpStatus.BAD_REQUEST);
            }

            // Adiciona o candidato à vaga
            vaga.getCandidatosInscritos().add(candidato);
            vagaService.salvarVaga(vaga);

            return new ResponseEntity<>("Candidato inscrito com sucesso!", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
