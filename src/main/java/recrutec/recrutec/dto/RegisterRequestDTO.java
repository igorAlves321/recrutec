package recrutec.recrutec.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO base para requisições de registro.
 * Contém campos comuns para todos os tipos de usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(min = 10, max = 15, message = "Telefone deve ter entre 10 e 15 caracteres")
    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmarSenha;
}