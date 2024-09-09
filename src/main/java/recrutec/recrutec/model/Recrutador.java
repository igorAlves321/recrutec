package recrutec.recrutec.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Recrutador extends User {
    private String empresa;
}
