package br.com.fatec.equadras.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

// Record: classe simples só para devolver dados (não é tabela no banco)
@Schema(description = "Faturamento de uma quadra, somando apenas as reservas confirmadas")
public record ResumoFaturamento(
        @Schema(example = "1") Long idQuadra,
        @Schema(example = "Arena Society 1") String quadra,
        @Schema(example = "4") long reservasConfirmadas,
        @Schema(example = "1") long reservasCanceladas,
        @Schema(example = "7") long horasVendidas,
        @Schema(example = "1050.00") BigDecimal faturamentoTotal
) {
}
