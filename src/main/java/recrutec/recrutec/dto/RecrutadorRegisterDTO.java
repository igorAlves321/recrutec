package recrutec.recrutec.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO para registro de recrutadores.
 * Estende RegisterRequestDTO com campos específicos de recrutadores.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecrutadorRegisterDTO extends RegisterRequestDTO {

    @NotBlank(message = "Nome da empresa é obrigatório")
    @Size(min = 2, max = 100, message = "Nome da empresa deve ter entre 2 e 100 caracteres")
    private String empresa;
}