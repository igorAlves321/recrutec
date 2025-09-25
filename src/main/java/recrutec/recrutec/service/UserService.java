package recrutec.recrutec.service;

import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Interface simplificada para serviço de usuários.
 * Trabalha apenas com a entidade User e usa Role para diferenciar comportamentos.
 *
 * Princípios aplicados:
 * - Single Responsibility: Responsável apenas por operações de usuários
 * - Simplicity: Uma única interface para todos os tipos de usuário
 * - DRY: Elimina duplicação de interfaces específicas por tipo
 */
public interface UserService {

    /**
     * Salva ou atualiza um usuário
     *
     * @param user Usuário a ser salvo
     * @return Usuário salvo com ID gerado
     */
    User save(User user);

    /**
     * Lista todos os usuários
     *
     * @return Lista de usuários
     */
    List<User> findAll();

    /**
     * Lista usuários por role
     *
     * @param role Role dos usuários
     * @return Lista de usuários com o role especificado
     */
    List<User> findByRole(Role role);

    /**
     * Busca usuário por ID
     *
     * @param id ID do usuário
     * @return Optional com o usuário encontrado
     */
    Optional<User> findById(Long id);

    /**
     * Busca usuário por email
     *
     * @param email Email do usuário
     * @return Optional com o usuário encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuário por email e senha (para login legado)
     *
     * @param email Email do usuário
     * @param senha Senha do usuário
     * @return Optional com o usuário encontrado
     */
    Optional<User> findByEmailAndSenha(String email, String senha);

    /**
     * Deleta usuário por ID
     *
     * @param id ID do usuário a ser deletado
     */
    void deleteById(Long id);

    /**
     * Verifica se existe usuário com o email
     *
     * @param email Email a ser verificado
     * @return true se existe, false caso contrário
     */
    boolean existsByEmail(String email);

    // Métodos específicos para candidatos

    /**
     * Busca candidatos por área de interesse
     *
     * @param area Área de interesse
     * @return Lista de candidatos
     */
    List<User> findCandidatosByAreaInteresse(String area);

    /**
     * Busca candidatos por habilidade
     *
     * @param habilidade Habilidade
     * @return Lista de candidatos
     */
    List<User> findCandidatosByHabilidade(String habilidade);

    // Métodos específicos para recrutadores

    /**
     * Busca recrutadores por empresa
     *
     * @param empresa Nome da empresa
     * @return Lista de recrutadores
     */
    List<User> findRecrutadoresByEmpresa(String empresa);

    // Métodos de conveniência

    /**
     * Lista apenas administradores
     *
     * @return Lista de administradores
     */
    List<User> findAdmins();

    /**
     * Lista apenas candidatos
     *
     * @return Lista de candidatos
     */
    List<User> findCandidatos();

    /**
     * Lista apenas recrutadores
     *
     * @return Lista de recrutadores
     */
    List<User> findRecrutadores();

    /**
     * Conta usuários por role
     *
     * @param role Role a ser contado
     * @return Quantidade de usuários
     */
    long countByRole(Role role);

    // Métodos de registro

    /**
     * Registra um novo candidato no sistema
     *
     * @param candidatoData Dados do candidato
     * @return Usuário candidato registrado
     * @throws IllegalArgumentException Se email já existe ou senhas não coincidem
     */
    User registerCandidato(recrutec.recrutec.dto.CandidatoRegisterDTO candidatoData);

    /**
     * Registra um novo recrutador no sistema
     *
     * @param recrutadorData Dados do recrutador
     * @return Usuário recrutador registrado
     * @throws IllegalArgumentException Se email já existe ou senhas não coincidem
     */
    User registerRecrutador(recrutec.recrutec.dto.RecrutadorRegisterDTO recrutadorData);
}