package recrutec.recrutec.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
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
    private Recrutador recrutador;

    @ManyToMany(mappedBy = "vagasInscritas")
    private Set<Candidato> candidatos;

    private LocalDate dataPostagem;

    // Define a data de postagem como a data atual antes de persistir a entidade
    @PrePersist
    public void prePersist() {
        this.dataPostagem = LocalDate.now();
    }

    // Método para calcular os dias desde a postagem
    public long getDiasDesdePostagem() {
        return ChronoUnit.DAYS.between(this.dataPostagem, LocalDate.now());
    }
}
