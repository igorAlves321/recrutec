package recrutec.recrutec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.model.Recrutador;
import recrutec.recrutec.service.RecrutadorService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recrutadores")
public class RecrutadorController {

    @Autowired
    private RecrutadorService recrutadorService;

    // Endpoint para listar todos os recrutadores
    @GetMapping
    public ResponseEntity<List<Recrutador>> listarRecrutadores() {
        List<Recrutador> recrutadores = recrutadorService.listarRecrutadores();
        return new ResponseEntity<>(recrutadores, HttpStatus.OK);
    }

    // Endpoint para buscar recrutador por ID
    @GetMapping("/{id}")
    public ResponseEntity<Recrutador> buscarRecrutadorPorId(@PathVariable Long id) {
        Optional<Recrutador> recrutador = recrutadorService.buscarRecrutadorPorId(id);
        if (recrutador.isPresent()) {
            return new ResponseEntity<>(recrutador.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para salvar um novo recrutador
    @PostMapping
    public ResponseEntity<Recrutador> salvarRecrutador(@RequestBody Recrutador recrutador) {
        Recrutador novoRecrutador = recrutadorService.salvarRecrutador(recrutador);
        return new ResponseEntity<>(novoRecrutador, HttpStatus.CREATED);
    }

    // Endpoint para deletar um recrutador
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRecrutador(@PathVariable Long id) {
        Optional<Recrutador> recrutador = recrutadorService.buscarRecrutadorPorId(id);
        if (recrutador.isPresent()) {
            recrutadorService.deletarRecrutador(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
