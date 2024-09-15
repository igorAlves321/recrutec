package recrutec.recrutec.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recrutec.recrutec.model.Admin;
import recrutec.recrutec.repository.AdminRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    // Criar ou salvar um novo Admin
    public Admin saveAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    // Buscar Admin por ID
    public Optional<Admin> findById(Long id) {
        return adminRepository.findById(id);
    }

    // Listar todos os Admins
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    // Deletar Admin por ID
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}
