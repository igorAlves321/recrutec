package recrutec.recrutec.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import recrutec.recrutec.model.User;
import recrutec.recrutec.model.Vaga;
import recrutec.recrutec.service.VagaService;
import recrutec.recrutec.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para gerenciamento de vagas.
 * 
 * Demonstra como usar autenticação JWT com Spring Security:
 * - @PreAuthorize para controle de acesso baseado em roles
 * - SecurityContextHolder para obter dados do usuário autenticado
 * - Diferentes níveis de acesso para diferentes operações
 */
@RestController
@RequestMapping("/api/vagas")
public class VagaController {

    @Autowired
    private VagaService vagaService;

    @Autowired
    private UserService userService;

    /**
     * Obtém o usuário autenticado atual
     * 
     * @return Email do usuário autenticado ou null se não autenticado
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
     * 
     * @param role Role a ser verificada (ex: "ROLE_ADMIN")
     * @return true se o usuário tem a role, false caso contrário
     */
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.getAuthorities().stream()
                   .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }

    // ENDPOINTS PÚBLICOS - Não requerem autenticação

    /**
     * Endpoint público para listar vagas ativas
     * Disponível para todos os usuários (incluindo não autenticados)
     */
    @GetMapping("/public")
    public ResponseEntity<List<Vaga>> listarVagasPublicas() {
        List<Vaga> vagas = vagaService.listarVagasAbertas(); // Implementar este método no service
        return new ResponseEntity<>(vagas, HttpStatus.OK);
    }

    /**
     * Endpoint público para buscar vaga específica
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<Vaga> buscarVagaPublica(@PathVariable Long id) {
        Optional<Vaga> vaga = vagaService.buscarVagaPorId(id);
        return vaga.map(v -> new ResponseEntity<>(v, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // ENDPOINTS AUTENTICADOS

    /**
     * Endpoint para listar todas as vagas
     * Requer autenticação (qualquer usuário logado)
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RECRUTADOR') or hasRole('ROLE_CANDIDATO')")
    public ResponseEntity<List<Vaga>> listarVagas() {
        List<Vaga> vagas;
        
        if (hasRole("ROLE_ADMIN")) {
            // Admin vê todas as vagas
            vagas = vagaService.listarVagas();
        } else if (hasRole("ROLE_RECRUTADOR")) {
            // Recrutador vê apenas suas vagas
            String email = getAuthenticatedUserEmail();
            vagas = vagaService.listarVagasPorRecrutadorEmail(email); // Implementar este método
        } else {
            // Candidatos veem apenas vagas abertas
            vagas = vagaService.listarVagasAbertas();
        }
        
        return new ResponseEntity<>(vagas, HttpStatus.OK);
    }

    /**
     * Endpoint para criar nova vaga
     * Apenas ADMINs e RECRUTADOREs podem criar vagas
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RECRUTADOR')")
    public ResponseEntity<Vaga> criarVaga(@RequestBody Vaga vaga) {
        String email = getAuthenticatedUserEmail();
        
        if (hasRole("ROLE_ADMIN")) {
            // Admin pode criar vaga para qualquer recrutador
            // Neste caso, deve ser informado o recrutadorId no corpo da requisição
            if (vaga.getRecrutador() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            // Recrutador cria vaga para si mesmo
            Optional<User> recrutador = userService.findByEmail(email);
            if (recrutador.isEmpty() || !recrutador.get().isRecrutador()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            vaga.setRecrutador(recrutador.get());
        }
        
        Vaga novaVaga = vagaService.salvarVaga(vaga);
        return new ResponseEntity<>(novaVaga, HttpStatus.CREATED);
    }

    /**
     * Endpoint para atualizar vaga
     * Apenas o recrutador dono da vaga ou ADMINs podem atualizar
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RECRUTADOR')")
    public ResponseEntity<Vaga> atualizarVaga(@PathVariable Long id, @RequestBody Vaga vagaAtualizada) {
        Optional<Vaga> vagaOptional = vagaService.buscarVagaPorId(id);
        
        if (vagaOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Vaga vagaExistente = vagaOptional.get();
        String emailUsuario = getAuthenticatedUserEmail();
        
        // Verifica autorização
        boolean isAdmin = hasRole("ROLE_ADMIN");
        boolean isOwner = vagaExistente.getRecrutador().getEmail().equals(emailUsuario);
        
        if (!isAdmin && !isOwner) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        // Atualiza apenas os campos editáveis
        vagaExistente.setTitulo(vagaAtualizada.getTitulo());
        vagaExistente.setDescricao(vagaAtualizada.getDescricao());
        vagaExistente.setStatus(vagaAtualizada.getStatus());
        
        // Admin pode alterar o recrutador, mas recrutador não pode
        if (isAdmin && vagaAtualizada.getRecrutador() != null) {
            vagaExistente.setRecrutador(vagaAtualizada.getRecrutador());
        }
        
        Vaga vagaAtualizadaSalva = vagaService.salvarVaga(vagaExistente);
        return new ResponseEntity<>(vagaAtualizadaSalva, HttpStatus.OK);
    }

    /**
     * Endpoint para deletar vaga
     * Apenas ADMINs podem deletar vagas
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deletarVaga(@PathVariable Long id) {
        Optional<Vaga> vagaOptional = vagaService.buscarVagaPorId(id);
        
        if (vagaOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        vagaService.deletarVaga(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Endpoint para candidato se inscrever em vaga
     * Apenas CANDIDATOs podem se inscrever
     */
    @PostMapping("/{vagaId}/inscrever")
    @PreAuthorize("hasRole('ROLE_CANDIDATO')")
    public ResponseEntity<String> inscreverCandidato(@PathVariable Long vagaId) {
        String emailCandidato = getAuthenticatedUserEmail();

        Optional<Vaga> vagaOptional = vagaService.buscarVagaPorId(vagaId);
        Optional<User> candidatoOptional = userService.findByEmail(emailCandidato);

        if (vagaOptional.isEmpty() || candidatoOptional.isEmpty() || !candidatoOptional.get().isCandidato()) {
            return new ResponseEntity<>("Vaga ou candidato não encontrado", HttpStatus.NOT_FOUND);
        }

        Vaga vaga = vagaOptional.get();
        User candidato = candidatoOptional.get();
        
        // Verifica se a vaga está aberta
        if (!"Aberta".equals(vaga.getStatus())) {
            return new ResponseEntity<>("A vaga não está aberta para inscrições.", HttpStatus.BAD_REQUEST);
        }
        
        // Verifica se já está inscrito
        if (vaga.getCandidatosInscritos().contains(candidato)) {
            return new ResponseEntity<>("Você já está inscrito nesta vaga.", HttpStatus.BAD_REQUEST);
        }
        
        // Inscreve o candidato
        vaga.getCandidatosInscritos().add(candidato);
        vagaService.salvarVaga(vaga);
        
        return new ResponseEntity<>("Inscrição realizada com sucesso!", HttpStatus.OK);
    }

    /**
     * Endpoint para listar candidatos inscritos em uma vaga
     * Apenas o recrutador dono da vaga ou ADMINs podem ver
     */
    @GetMapping("/{vagaId}/candidatos")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RECRUTADOR')")
    public ResponseEntity<List<User>> listarCandidatosInscritos(@PathVariable Long vagaId) {
        Optional<Vaga> vagaOptional = vagaService.buscarVagaPorId(vagaId);
        
        if (vagaOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Vaga vaga = vagaOptional.get();
        String emailUsuario = getAuthenticatedUserEmail();
        
        // Verifica autorização
        boolean isAdmin = hasRole("ROLE_ADMIN");
        boolean isOwner = vaga.getRecrutador().getEmail().equals(emailUsuario);
        
        if (!isAdmin && !isOwner) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        return new ResponseEntity<>(vaga.getCandidatosInscritos().stream().toList(), HttpStatus.OK);
    }
}
