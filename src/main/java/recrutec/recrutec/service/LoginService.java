package recrutec.recrutec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recrutec.recrutec.model.Admin;
import recrutec.recrutec.model.Candidato;
import recrutec.recrutec.model.Recrutador;
import recrutec.recrutec.repository.AdminRepository;
import recrutec.recrutec.repository.CandidatoRepository;
import recrutec.recrutec.repository.RecrutadorRepository;

import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private RecrutadorRepository recrutadorRepository;

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Autowired
    private AdminRepository adminRepository;

    // Login para recrutadores
    public Optional<Recrutador> loginRecrutador(String email, String senha) {
        return recrutadorRepository.findByEmailAndSenha(email, senha);
    }

    // Login para candidatos
    public Optional<Candidato> loginCandidato(String email, String senha) {
        return candidatoRepository.findByEmailAndSenha(email, senha);
    }

    // Login para administradores
    public Optional<Admin> loginAdmin(String email, String senha) {
        return adminRepository.findByEmailAndSenha(email, senha);
    }
}
