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

    // Endpoint para atualizar um recrutador existente (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Recrutador> atualizarRecrutador(@PathVariable Long id, @RequestBody Recrutador recrutadorAtualizado) {
        Optional<Recrutador> recrutadorOptional = recrutadorService.buscarRecrutadorPorId(id);
        if (recrutadorOptional.isPresent()) {
            Recrutador recrutadorExistente = recrutadorOptional.get();
            recrutadorExistente.setNome(recrutadorAtualizado.getNome());
            recrutadorExistente.setEmail(recrutadorAtualizado.getEmail());
            recrutadorExistente.setEmpresa(recrutadorAtualizado.getEmpresa());
            // Atualizar outros campos conforme necessário
            Recrutador recrutadorAtualizadoSalvo = recrutadorService.salvarRecrutador(recrutadorExistente);
            return new ResponseEntity<>(recrutadorAtualizadoSalvo, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para deletar um recrutador (somente administrador ou o próprio recrutador)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRecrutador(@PathVariable Long id) {
        Long userId = getLoggedUserId();  // Obtenção simulada do ID do usuário logado
        String userRole = getLoggedUserRole();  // Obtenção simulada do papel do usuário logado

        System.out.println("Tentativa de exclusão. ID do recrutador: " + id + ", userId: " + userId + ", userRole: " + userRole);

        Optional<Recrutador> recrutadorOptional = recrutadorService.buscarRecrutadorPorId(id);

        if (recrutadorOptional.isPresent()) {
            Recrutador recrutadorExistente = recrutadorOptional.get();

            if ("ADMIN".equalsIgnoreCase(userRole)) {
                System.out.println("Usuário ADMIN autorizado a deletar.");
                recrutadorService.deletarRecrutador(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            if (recrutadorExistente.getId().equals(userId)) {
                System.out.println("Recrutador original autorizado a deletar.");
                recrutadorService.deletarRecrutador(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            System.out.println("Usuário não autorizado a deletar o recrutador.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        System.out.println("Recrutador não encontrado.");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}