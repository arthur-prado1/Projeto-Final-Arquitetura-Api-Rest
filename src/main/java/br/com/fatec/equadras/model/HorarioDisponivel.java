package br.com.fatec.equadras.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

// Record: classe simples só para devolver dados (não é tabela no banco)
@Schema(description = "Faixa de 1 hora da grade da quadra em um dia")
public record HorarioDisponivel(
        @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "19:00") LocalTime horaInicio,
        @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "20:00") LocalTime horaFim,
        @Schema(example = "false") boolean disponivel,
        @Schema(description = "Motivo quando indisponível", example = "Reservado") String situacao
) {
}
