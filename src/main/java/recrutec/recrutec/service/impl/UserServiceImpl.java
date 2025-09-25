package recrutec.recrutec.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import recrutec.recrutec.dto.CandidatoRegisterDTO;
import recrutec.recrutec.dto.RecrutadorRegisterDTO;
import recrutec.recrutec.exception.InvalidDataException;
import recrutec.recrutec.exception.ResourceAlreadyExistsException;
import recrutec.recrutec.exception.ResourceNotFoundException;
import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;
import recrutec.recrutec.repository.UserRepository;
import recrutec.recrutec.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Implementação simplificada do UserService.
 * Trabalha apenas com a entidade User, eliminando complexidade desnecessária.
 *
 * Princípios aplicados:
 * - Single Responsibility: Responsável apenas por operações CRUD de usuários
 * - DRY: Uma única implementação para todos os tipos de usuário
 * - Simplicity: Lógica centralizada e clara
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User save(User user) {
        log.debug("Salvando usuário: {} ({})", user.getEmail(), user.getRole());

        // Criptografar senha se necessário
        if (user.getSenha() != null && !user.getSenha().startsWith("$2a$")) {
            user.setSenha(passwordEncoder.encode(user.getSenha()));
        }

        User savedUser = userRepository.save(user);
        log.info("Usuário salvo com sucesso: {} (ID: {}, Role: {})",
                savedUser.getEmail(), savedUser.getId(), savedUser.getRole());

        return savedUser;
    }

    @Override
    public List<User> findAll() {
        log.debug("Listando todos os usuários");
        return userRepository.findAll();
    }

    @Override
    public List<User> findByRole(Role role) {
        log.debug("Listando usuários por role: {}", role);
        return userRepository.findByRole(role);
    }

    @Override
    public Optional<User> findById(Long id) {
        log.debug("Buscando usuário por ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Buscando usuário por email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByEmailAndSenha(String email, String senha) {
        log.debug("Buscando usuário por email e senha: {}", email);
        return userRepository.findByEmailAndSenha(email, senha);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deletando usuário por ID: {}", id);
        userRepository.deleteById(id);
        log.info("Usuário deletado com sucesso: ID {}", id);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Verificando se existe usuário com email: {}", email);
        return userRepository.existsByEmail(email);
    }

    // Métodos específicos para candidatos

    @Override
    public List<User> findCandidatosByAreaInteresse(String area) {
        log.debug("Buscando candidatos por área de interesse: {}", area);
        return userRepository.findCandidatosByAreaInteresse(area);
    }

    @Override
    public List<User> findCandidatosByHabilidade(String habilidade) {
        log.debug("Buscando candidatos por habilidade: {}", habilidade);
        return userRepository.findCandidatosByHabilidade(habilidade);
    }

    // Métodos específicos para recrutadores

    @Override
    public List<User> findRecrutadoresByEmpresa(String empresa) {
        log.debug("Buscando recrutadores por empresa: {}", empresa);
        return userRepository.findRecrutadoresByEmpresa(empresa);
    }

    // Métodos de conveniência

    @Override
    public List<User> findAdmins() {
        log.debug("Listando administradores");
        return userRepository.findAdmins();
    }

    @Override
    public List<User> findCandidatos() {
        log.debug("Listando candidatos");
        return userRepository.findCandidatos();
    }

    @Override
    public List<User> findRecrutadores() {
        log.debug("Listando recrutadores");
        return userRepository.findRecrutadores();
    }

    @Override
    public long countByRole(Role role) {
        log.debug("Contando usuários por role: {}", role);
        return userRepository.countByRole(role);
    }

    /**
     * Cria o primeiro admin do sistema se não existir nenhum
     */
    public void createDefaultAdminIfNotExists() {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            log.info("Nenhum admin encontrado, criando admin padrão");

            User defaultAdmin = new User();
            defaultAdmin.setNome("Administrador");
            defaultAdmin.setEmail("admin@recrutec.com");
            defaultAdmin.setSenha("admin123"); // Será criptografada pelo save()
            defaultAdmin.setTelefone("(00) 00000-0000");
            defaultAdmin.setRole(Role.ADMIN);

            save(defaultAdmin);

            log.info("Admin padrão criado: admin@recrutec.com / admin123");
        }
    }

    // Implementação dos métodos de registro

    @Override
    public User registerCandidato(CandidatoRegisterDTO candidatoData) {
        log.debug("Iniciando registro de candidato: {}", candidatoData.getEmail());

        // Validações
        validateRegistration(candidatoData.getEmail(), candidatoData.getSenha(), candidatoData.getConfirmarSenha());

        // Cria novo candidato
        User candidato = new User();
        candidato.setNome(candidatoData.getNome());
        candidato.setEmail(candidatoData.getEmail());
        candidato.setTelefone(candidatoData.getTelefone());
        candidato.setSenha(candidatoData.getSenha());
        candidato.setRole(Role.CANDIDATO);

        // Campos específicos de candidato
        candidato.setCurriculo(candidatoData.getCurriculo());
        candidato.setAreaInteresse(candidatoData.getAreaInteresse());
        candidato.setHabilidades(candidatoData.getHabilidades());
        candidato.setCertificados(candidatoData.getCertificados());
        candidato.setPcd(candidatoData.getPcd());

        // Salva no banco
        User savedCandidato = save(candidato);

        log.info("Candidato registrado com sucesso: {} (ID: {})",
                savedCandidato.getEmail(), savedCandidato.getId());

        return savedCandidato;
    }

    @Override
    public User registerRecrutador(RecrutadorRegisterDTO recrutadorData) {
        log.debug("Iniciando registro de recrutador: {}", recrutadorData.getEmail());

        // Validações
        validateRegistration(recrutadorData.getEmail(), recrutadorData.getSenha(), recrutadorData.getConfirmarSenha());

        // Cria novo recrutador
        User recrutador = new User();
        recrutador.setNome(recrutadorData.getNome());
        recrutador.setEmail(recrutadorData.getEmail());
        recrutador.setTelefone(recrutadorData.getTelefone());
        recrutador.setSenha(recrutadorData.getSenha());
        recrutador.setRole(Role.RECRUTADOR);

        // Campos específicos de recrutador
        recrutador.setEmpresa(recrutadorData.getEmpresa());

        // Salva no banco
        User savedRecrutador = save(recrutador);

        log.info("Recrutador registrado com sucesso: {} (ID: {})",
                savedRecrutador.getEmail(), savedRecrutador.getId());

        return savedRecrutador;
    }

    /**
     * Valida dados de registro
     *
     * @param email Email do usuário
     * @param senha Senha fornecida
     * @param confirmarSenha Confirmação da senha
     * @throws IllegalArgumentException Se validação falhar
     */
    private void validateRegistration(String email, String senha, String confirmarSenha) {
        // Verifica se email já existe
        if (existsByEmail(email)) {
            log.warn("Tentativa de registro com email já existente: {}", email);
            throw new ResourceAlreadyExistsException("Usuário", "email", email);
        }

        // Verifica se senhas coincidem
        if (!senha.equals(confirmarSenha)) {
            log.warn("Senhas não coincidem para email: {}", email);
            throw new InvalidDataException("Senhas não coincidem");
        }

        log.debug("Validação de registro aprovada para email: {}", email);
    }
}