package recrutec.recrutec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de registro de usuário bem-sucedido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String role;
    private String message;

    public UserRegisterResponseDTO(Long id, String nome, String email, String role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.message = "Usuário cadastrado com sucesso!";
    }
}