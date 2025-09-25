package recrutec.recrutec.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * DTO para registro de candidatos.
 * Estende RegisterRequestDTO com campos específicos de candidatos.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CandidatoRegisterDTO extends RegisterRequestDTO {

    private String curriculo;

    @NotNull(message = "Áreas de interesse são obrigatórias")
    private List<String> areaInteresse;

    private List<String> habilidades;

    private List<String> certificados;

    private String pcd; // Pessoa com Deficiência
}