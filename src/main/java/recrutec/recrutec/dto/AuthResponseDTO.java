package recrutec.recrutec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de autenticação bem-sucedida.
 * Contém os tokens JWT e informações básicas do usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfoDTO user;

    public AuthResponseDTO(String accessToken, String refreshToken, Long expiresIn, UserInfoDTO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
        this.tokenType = "Bearer";
    }

    /**
     * DTO com informações básicas do usuário autenticado
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Long id;
        private String nome;
        private String email;
        private String role;
    }
}
