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

    // Simulação para obter o ID do usuário logado
    private Long getLoggedUserId() {
        // Simular um usuário logado (ex: o ID do recrutador ou admin)
        return 1L; // Exemplo: Retorna o ID do usuário logado
    }

    // Simulação para obter o papel do usuário logado
    private String getLoggedUserRole() {
        // Simular um papel de usuário (ADMIN ou RECRUTADOR)
        return "ADMIN"; // Exemplo: Retorna o papel do usuário logado
    }

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
        return candidato.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Endpoint para salvar um novo candidato
    @PostMapping
    public ResponseEntity<Candidato> salvarCandidato(@RequestBody Candidato candidato) {
        Candidato novoCandidato = candidatoService.salvarCandidato(candidato);
        return new ResponseEntity<>(novoCandidato, HttpStatus.CREATED);
    }

    // Endpoint para atualizar um candidato existente (somente administrador)
    @PutMapping("/{id}")
    public ResponseEntity<Candidato> atualizarCandidato(@PathVariable Long id, @RequestBody Candidato candidatoAtualizado) {
        String role = getLoggedUserRole(); // Obtenção simulada do papel do usuário logado

        // Verificação de permissões
        if (!"ADMIN".equals(role)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Retorna 403 se não for ADMIN
        }

        Optional<Candidato> candidatoOptional = candidatoService.buscarCandidatoPorId(id);
        if (candidatoOptional.isPresent()) {
            Candidato candidatoExistente = candidatoOptional.get();
            candidatoExistente.setNome(candidatoAtualizado.getNome());
            candidatoExistente.setEmail(candidatoAtualizado.getEmail());
            candidatoExistente.setCurriculo(candidatoAtualizado.getCurriculo());
            candidatoExistente.setAreaInteresse(candidatoAtualizado.getAreaInteresse());
            // Atualizar outros campos conforme necessário
            Candidato candidatoAtualizadoSalvo = candidatoService.salvarCandidato(candidatoExistente);
            return new ResponseEntity<>(candidatoAtualizadoSalvo, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para deletar um candidato (somente administrador)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCandidato(@PathVariable Long id) {
        String role = getLoggedUserRole(); // Obtenção simulada do papel do usuário logado

        // Verificação de permissões
        if (!"ADMIN".equals(role)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // Retorna 403 se não for ADMIN
        }

        Optional<Candidato> candidato = candidatoService.buscarCandidatoPorId(id);
        if (candidato.isPresent()) {
            candidatoService.deletarCandidato(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}