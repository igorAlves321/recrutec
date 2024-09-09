package recrutec.recrutec.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class User {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String senha;
}

