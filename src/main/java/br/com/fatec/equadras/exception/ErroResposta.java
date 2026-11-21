package br.com.fatec.equadras.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

// Formato padrão de TODOS os erros devolvidos pela API
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Formato padrão de erro da API")
public record ErroResposta(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") @Schema(type = "string", example = "2026-11-21T14:30:00") LocalDateTime dataHora,
        @Schema(example = "409") int status,
        @Schema(example = "Conflict") String erro,
        @Schema(example = "Horário indisponível: a quadra já está reservada das 19:00 às 20:00 nesta data.") String mensagem,
        @Schema(example = "/reserva") String caminho,
        @Schema(description = "Preenchido apenas em erros de validação de campos") List<CampoInvalido> campos
) {
    @Schema(description = "Campo recusado pela validação")
    public record CampoInvalido(
            @Schema(example = "cpf") String campo,
            @Schema(example = "CPF inválido.") String mensagem
    ) {
    }
}
