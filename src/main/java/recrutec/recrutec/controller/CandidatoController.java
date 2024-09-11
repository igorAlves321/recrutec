package recrutec.recrutec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.model.Candidato;
import recrutec.recrutec.service.CandidatoService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/candidatos")
public class CandidatoController {

    @Autowired
    private CandidatoService candidatoService;

    // Endpoint para listar todos os candidatos
    @GetMapping
    public ResponseEntity<List<Candidato>> listarCandidatos() {
        List<Candidato> candidatos = candidatoService.listarCandidatos();
        return new ResponseEntity<>(candidatos, HttpStatus.OK);
    }

    // Endpoint para buscar candidato por ID
    @GetMapping("/{id}")
    public ResponseEntity<Candidato> buscarCandidatoPorId(@PathVariable Long id) {
        Optional<Candidato> candidato = candidatoService.buscarCandidatoPorId(id);
        if (candidato.isPresent()) {
            return new ResponseEntity<>(candidato.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para salvar um novo candidato
    @PostMapping
    public ResponseEntity<Candidato> salvarCandidato(@RequestBody Candidato candidato) {
        Candidato novoCandidato = candidatoService.salvarCandidato(candidato);
        return new ResponseEntity<>(novoCandidato, HttpStatus.CREATED);
    }

    // Endpoint para deletar um candidato
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCandidato(@PathVariable Long id) {
        Optional<Candidato> candidato = candidatoService.buscarCandidatoPorId(id);
        if (candidato.isPresent()) {
            candidatoService.deletarCandidato(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
