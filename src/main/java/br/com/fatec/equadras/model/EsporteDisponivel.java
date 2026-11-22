package br.com.fatec.equadras.model;

import io.swagger.v3.oas.annotations.media.Schema;

// Record: classe simples só para devolver dados (não é tabela no banco)
@Schema(description = "Tipo de esporte aceito pela API")
public record EsporteDisponivel(
        @Schema(description = "Valor a ser enviado no campo tipoEsporte", example = "BEACH_TENNIS") String codigo,
        @Schema(description = "Nome para exibir", example = "Beach Tennis") String nome
) {
}
