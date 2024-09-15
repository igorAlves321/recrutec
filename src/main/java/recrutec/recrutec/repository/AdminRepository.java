package recrutec.recrutec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import recrutec.recrutec.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
