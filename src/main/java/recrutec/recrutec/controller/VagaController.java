package recrutec.recrutec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.model.Vaga;
import recrutec.recrutec.model.Recrutador;
import recrutec.recrutec.service.VagaService;
import recrutec.recrutec.service.RecrutadorService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vagas")
public class VagaController {

    @Autowired
    private VagaService vagaService;

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
        if (vaga.isPresent()) {
            return new ResponseEntity<>(vaga.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para salvar uma nova vaga, associando ao recrutador
    @PostMapping
    public ResponseEntity<Vaga> salvarVaga(@RequestBody Vaga vaga, @RequestParam Long recrutadorId) {
        Optional<Recrutador> recrutador = recrutadorService.buscarRecrutadorPorId(recrutadorId);
        
        if (recrutador.isPresent()) {
            vaga.setRecrutador(recrutador.get());  // Associa o recrutador à vaga
            Vaga novaVaga = vagaService.salvarVaga(vaga);
            return new ResponseEntity<>(novaVaga, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Recrutador não encontrado
        }
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
}
