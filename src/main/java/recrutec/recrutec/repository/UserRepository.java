package recrutec.recrutec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository único para todas as operações de usuários.
 *
 * Princípios aplicados:
 * - Single Responsibility: Responsável apenas por operações de dados de User
 * - DRY: Elimina duplicação dos 3 repositories anteriores
 * - Query Methods: Métodos baseados em convenção do Spring Data
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar por email (para login e validações)
    Optional<User> findByEmail(String email);

    // Buscar por email e senha (para autenticação legada, se necessário)
    Optional<User> findByEmailAndSenha(String email, String senha);

    // Buscar usuários por role
    List<User> findByRole(Role role);

    // Buscar administradores
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAdmins();

    // Buscar candidatos
    @Query("SELECT u FROM User u WHERE u.role = 'CANDIDATO'")
    List<User> findCandidatos();

    // Buscar recrutadores
    @Query("SELECT u FROM User u WHERE u.role = 'RECRUTADOR'")
    List<User> findRecrutadores();

    // Buscar candidatos por área de interesse
    @Query("SELECT DISTINCT u FROM User u JOIN u.areaInteresse a WHERE u.role = 'CANDIDATO' AND a LIKE %:area%")
    List<User> findCandidatosByAreaInteresse(@Param("area") String area);

    // Buscar candidatos por habilidade
    @Query("SELECT DISTINCT u FROM User u JOIN u.habilidades h WHERE u.role = 'CANDIDATO' AND h LIKE %:habilidade%")
    List<User> findCandidatosByHabilidade(@Param("habilidade") String habilidade);

    // Buscar recrutadores por empresa
    @Query("SELECT u FROM User u WHERE u.role = 'RECRUTADOR' AND u.empresa LIKE %:empresa%")
    List<User> findRecrutadoresByEmpresa(@Param("empresa") String empresa);

    // Verificar se email já existe
    boolean existsByEmail(String email);

    // Contar usuários por role
    long countByRole(Role role);

    // Buscar usuários por nome (pesquisa)
    @Query("SELECT u FROM User u WHERE u.nome LIKE %:nome%")
    List<User> findByNomeContaining(@Param("nome") String nome);

    // Buscar por email ou nome (pesquisa geral)
    @Query("SELECT u FROM User u WHERE u.email LIKE %:termo% OR u.nome LIKE %:termo%")
    List<User> findByEmailOrNomeContaining(@Param("termo") String termo);
}