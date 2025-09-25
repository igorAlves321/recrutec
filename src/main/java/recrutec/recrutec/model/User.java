package recrutec.recrutec.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Entidade única User que representa todos os tipos de usuários do sistema.
 * Usa Role enum para diferenciar entre ADMIN, CANDIDATO e RECRUTADOR.
 *
 * Princípios aplicados:
 * - Single Table Strategy: Mais simples e performático
 * - Campos condicionais: Baseados no role do usuário
 * - Eliminação de herança desnecessária
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String telefone;

    @JsonIgnore // Nunca retornar senha em APIs
    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Campos específicos para CANDIDATOS
    @Column(length = 500)
    private String curriculo; // URL do currículo

    @ElementCollection
    @CollectionTable(name = "user_areas_interesse", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "area")
    private List<String> areaInteresse;

    @ElementCollection
    @CollectionTable(name = "user_habilidades", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "habilidade")
    private List<String> habilidades;

    @ElementCollection
    @CollectionTable(name = "user_certificados", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "certificado")
    private List<String> certificados;

    @Column(length = 50)
    private String pcd; // Pessoa com Deficiência

    // Campos específicos para RECRUTADORES
    @Column(length = 100)
    private String empresa;

    // Métodos de conveniência para verificar roles
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isCandidato() {
        return Role.CANDIDATO.equals(this.role);
    }

    public boolean isRecrutador() {
        return Role.RECRUTADOR.equals(this.role);
    }

    // Validações de campos baseadas no role
    @PrePersist
    @PreUpdate
    private void validateFieldsByRole() {
        if (this.role == null) {
            throw new IllegalStateException("Role é obrigatório");
        }

        // Limpar campos não aplicáveis baseado no role
        switch (this.role) {
            case ADMIN:
                // Admin não precisa de dados específicos de candidato ou recrutador
                this.curriculo = null;
                this.areaInteresse = null;
                this.habilidades = null;
                this.certificados = null;
                this.pcd = null;
                this.empresa = null;
                break;
            case CANDIDATO:
                // Candidato não precisa de dados de recrutador
                this.empresa = null;
                break;
            case RECRUTADOR:
                // Recrutador não precisa de dados de candidato
                this.curriculo = null;
                this.areaInteresse = null;
                this.habilidades = null;
                this.certificados = null;
                this.pcd = null;
                break;
        }
    }
}
