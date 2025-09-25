package recrutec.recrutec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de refresh token.
 * Usado para renovar o access token usando um refresh token válido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token é obrigatório")
    private String refreshToken;
}
