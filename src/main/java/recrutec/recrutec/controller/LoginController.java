package recrutec.recrutec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.model.Admin;
import recrutec.recrutec.model.Recrutador;
import recrutec.recrutec.model.Candidato;
import recrutec.recrutec.service.LoginService;

import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    // Endpoint para login de recrutadores
    @PostMapping("/recrutador")
    public ResponseEntity<Recrutador> loginRecrutador(@RequestBody LoginRequest loginRequest) {
        Optional<Recrutador> recrutador = loginService.loginRecrutador(loginRequest.getEmail(), loginRequest.getSenha());
        if (recrutador.isPresent()) {
            return ResponseEntity.ok(recrutador.get());
        } else {
            return ResponseEntity.status(401).build();  // Não autorizado
        }
    }

    // Endpoint para login de candidatos
    @PostMapping("/candidato")
    public ResponseEntity<Candidato> loginCandidato(@RequestBody LoginRequest loginRequest) {
        Optional<Candidato> candidato = loginService.loginCandidato(loginRequest.getEmail(), loginRequest.getSenha());
        if (candidato.isPresent()) {
            return ResponseEntity.ok(candidato.get());
        } else {
            return ResponseEntity.status(401).build();  // Não autorizado
        }
    }

    // Endpoint para login de administradores
    @PostMapping("/admin")
    public ResponseEntity<Admin> loginAdmin(@RequestBody LoginRequest loginRequest) {
        Optional<Admin> admin = loginService.loginAdmin(loginRequest.getEmail(), loginRequest.getSenha());
        if (admin.isPresent()) {
            return ResponseEntity.ok(admin.get());
        } else {
            return ResponseEntity.status(401).build();  // Não autorizado
        }
    }
}

// Classe auxiliar para capturar o e-mail e senha do request
class LoginRequest {
    private String email;
    private String senha;

    // Getters e setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
