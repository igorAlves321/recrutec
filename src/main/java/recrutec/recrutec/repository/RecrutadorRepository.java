package recrutec.recrutec.repository;

import recrutec.recrutec.model.Recrutador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecrutadorRepository extends JpaRepository<Recrutador, Long> {
    Optional<Recrutador> findByEmailAndSenha(String email, String senha);    
}
