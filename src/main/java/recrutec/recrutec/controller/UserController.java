package recrutec.recrutec.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.model.Role;
import recrutec.recrutec.model.User;
import recrutec.recrutec.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Controlador unificado para gerenciamento de usuários.
 *
 * Princípios aplicados:
 * - Single Responsibility: Responsável apenas por endpoints de usuários
 * - DRY: Um único controller para todos os tipos de usuário
 * - Role-based Access: Usa Role enum para controle de acesso
 * - RESTful: Segue padrões REST adequados
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class UserController {

    private final UserService userService;

    /**
     * Obtém o usuário autenticado atual
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }

    /**
     * Verifica se o usuário tem uma role específica
     */
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.getAuthorities().stream()
                   .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    // ENDPOINTS GERAIS

    /**
     * Buscar próprio perfil (qualquer usuário autenticado)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getMyProfile() {
        String email = getAuthenticatedUserEmail();
        log.debug("Usuário buscando próprio perfil: {}", email);

        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Atualizar próprio perfil (qualquer usuário autenticado)
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateMyProfile(@RequestBody User updatedUser) {
        String email = getAuthenticatedUserEmail();
        log.debug("Usuário atualizando próprio perfil: {}", email);

        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = userOptional.get();

        // Campos que todos podem atualizar
        existingUser.setNome(updatedUser.getNome());
        existingUser.setTelefone(updatedUser.getTelefone());

        // Campos específicos por role
        if (existingUser.isCandidato()) {
            existingUser.setCurriculo(updatedUser.getCurriculo());
            existingUser.setAreaInteresse(updatedUser.getAreaInteresse());
            existingUser.setHabilidades(updatedUser.getHabilidades());
            existingUser.setCertificados(updatedUser.getCertificados());
            existingUser.setPcd(updatedUser.getPcd());
        } else if (existingUser.isRecrutador()) {
            existingUser.setEmpresa(updatedUser.getEmpresa());
        }

        // Email e senha não podem ser alterados aqui (endpoints específicos)
        User savedUser = userService.save(existingUser);
        log.info("Usuário atualizou próprio perfil: {}", email);

        return ResponseEntity.ok(savedUser);
    }

    // ENDPOINTS ADMINISTRATIVOS

    /**
     * Listar todos os usuários (apenas admins)
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.debug("Admin listando todos os usuários");
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Listar usuários por role (admins e recrutadores para candidatos)
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_RECRUTADOR') and #role == 'CANDIDATO')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        log.debug("Listando usuários por role: {}", role);

        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            List<User> users = userService.findByRole(roleEnum);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Buscar usuário por ID (admins ou próprio usuário)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.findById(#id).orElse(new recrutec.recrutec.model.User()).email == authentication.name")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.debug("Buscando usuário por ID: {}", id);

        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Atualizar usuário por ID (apenas admins)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        String adminEmail = getAuthenticatedUserEmail();
        log.debug("Admin {} atualizando usuário ID: {}", adminEmail, id);

        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = userOptional.get();

        // Admin pode alterar tudo exceto ID e senha (endpoint específico para senha)
        existingUser.setNome(updatedUser.getNome());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setTelefone(updatedUser.getTelefone());
        existingUser.setRole(updatedUser.getRole());

        // Campos específicos por role
        existingUser.setCurriculo(updatedUser.getCurriculo());
        existingUser.setAreaInteresse(updatedUser.getAreaInteresse());
        existingUser.setHabilidades(updatedUser.getHabilidades());
        existingUser.setCertificados(updatedUser.getCertificados());
        existingUser.setPcd(updatedUser.getPcd());
        existingUser.setEmpresa(updatedUser.getEmpresa());

        User savedUser = userService.save(existingUser);
        log.info("Admin {} atualizou usuário: {}", adminEmail, id);

        return ResponseEntity.ok(savedUser);
    }

    /**
     * Deletar usuário (apenas admins)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        String adminEmail = getAuthenticatedUserEmail();
        log.debug("Admin {} deletando usuário ID: {}", adminEmail, id);

        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteById(id);
        log.info("Admin {} deletou usuário: {}", adminEmail, id);

        return ResponseEntity.noContent().build();
    }

    // ENDPOINTS ESPECÍFICOS PARA CANDIDATOS

    /**
     * Buscar candidatos por área de interesse (recrutadores e admins)
     */
    @GetMapping("/candidatos/area/{area}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RECRUTADOR')")
    public ResponseEntity<List<User>> getCandidatesByArea(@PathVariable String area) {
        log.debug("Buscando candidatos por área de interesse: {}", area);
        List<User> candidatos = userService.findCandidatosByAreaInteresse(area);
        return ResponseEntity.ok(candidatos);
    }

    /**
     * Buscar candidatos por habilidade (recrutadores e admins)
     */
    @GetMapping("/candidatos/habilidade/{habilidade}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RECRUTADOR')")
    public ResponseEntity<List<User>> getCandidatesBySkill(@PathVariable String habilidade) {
        log.debug("Buscando candidatos por habilidade: {}", habilidade);
        List<User> candidatos = userService.findCandidatosByHabilidade(habilidade);
        return ResponseEntity.ok(candidatos);
    }

    // ENDPOINTS ESPECÍFICOS PARA RECRUTADORES

    /**
     * Buscar recrutadores por empresa (apenas admins)
     */
    @GetMapping("/recrutadores/empresa/{empresa}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getRecruitersByCompany(@PathVariable String empresa) {
        log.debug("Buscando recrutadores por empresa: {}", empresa);
        List<User> recrutadores = userService.findRecrutadoresByEmpresa(empresa);
        return ResponseEntity.ok(recrutadores);
    }

    // ENDPOINTS DE ESTATÍSTICAS

    /**
     * Contar usuários por role (apenas admins)
     */
    @GetMapping("/stats/count/{role}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Long> countUsersByRole(@PathVariable String role) {
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            long count = userService.countByRole(roleEnum);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}