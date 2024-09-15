package recrutec.recrutec.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Candidato extends User {

    private String curriculo;

    @ElementCollection
    private List<String> areaInteresse;

    @ElementCollection
    private List<String> habilidades;

    @ElementCollection
    private List<String> certificados;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "candidato_vaga",
        joinColumns = @JoinColumn(name = "candidato_id"),
        inverseJoinColumns = @JoinColumn(name = "vaga_id")
    )
    private Set<Vaga> vagasInscritas;
    private String pcd;
}
