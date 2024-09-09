package recrutec.recrutec.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Candidato extends User {
    private String curriculo;
    private String areaInteresse;
}
