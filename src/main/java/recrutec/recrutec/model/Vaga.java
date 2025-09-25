package recrutec.recrutec.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Getter
@Setter
@Entity
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descricao;
    private String status;

    @ManyToOne
    private User recrutador; // Deve ser um User com role RECRUTADOR

    @ManyToMany
    @JoinTable(
        name = "inscricoes",
        joinColumns = @JoinColumn(name = "vaga_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> candidatosInscritos; // Users com role CANDIDATO

    private LocalDate dataPostagem;

    // Define a data de postagem como a data atual antes de persistir a entidade
    @PrePersist
    public void prePersist() {
        if (this.dataPostagem == null) {
            this.dataPostagem = LocalDate.now();
        }
    }

    // Método para calcular os dias desde a postagem
    public long getDiasDesdePostagem() {
        if (this.dataPostagem != null) {
            return ChronoUnit.DAYS.between(this.dataPostagem, LocalDate.now());
        }
        return 0; // Retorna 0 se a data não estiver definida
    }
}
