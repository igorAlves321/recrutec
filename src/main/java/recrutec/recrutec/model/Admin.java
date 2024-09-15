package recrutec.recrutec.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Admin extends User {

    public Admin() {
        this.setRole(Role.ADMIN); // Definindo o papel como ADMIN
    }

}
