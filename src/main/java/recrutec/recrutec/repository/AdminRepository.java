package recrutec.recrutec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import recrutec.recrutec.model.Admin;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmailAndSenha(String email, String senha);
}
