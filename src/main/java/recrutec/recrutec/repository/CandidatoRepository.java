package recrutec.recrutec.repository;

import recrutec.recrutec.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {
    
}
