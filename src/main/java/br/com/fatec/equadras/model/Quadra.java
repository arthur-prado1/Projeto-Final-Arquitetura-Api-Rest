package br.com.fatec.equadras.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Entity
@Table(name="quadra")
@Valid // @Valid: usado para implementar todas as outras validações
@JsonPropertyOrder({"id_quadra", "nome", "tipoEsporte", "valorHora", "endereco", "horaAbertura", "horaFechamento", "ativa"})
@Schema(description = "Quadra esportiva disponível para aluguel")
public class Quadra {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Schema(description = "Gerado automaticamente pelo banco (ignorado no cadastro)", example = "1")
    private Long id_quadra;

    @Column (name = "nome_quadra", nullable=false, unique=true)
    @NotBlank(message = "O nome é obrigatório.")
    @Size (min = 3, max = 80, message = "Nome deve ter entre 3 e 80 caracteres.")
    @Schema(example = "Arena Society 1")
    private String nome;

    // EnumType.STRING: grava o nome do esporte no banco (ex.: "FUTSAL") em vez de um número
    @Enumerated(EnumType.STRING)
    @Column (name = "tipo_esporte_quadra", nullable=false)
    @NotNull(message = "O tipo de esporte é obrigatório.")
    @Schema(example = "FUTEBOL_SOCIETY")
    private TipoEsporte tipoEsporte;

    // BigDecimal: tipo indicado para valores em dinheiro (não perde centavos em arredondamentos)
    @Column (name = "valor_hora_quadra", nullable=false, precision = 10, scale = 2)
    @NotNull(message = "O valor da hora é obrigatório.")
    @Positive(message = "O valor da hora deve ser maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "Valor da hora deve ter no máximo 2 casas decimais.")
    @Schema(description = "Preço cobrado por hora (R$)", example = "150.00")
    private BigDecimal valorHora;

    @Column (name = "endereco_quadra", nullable=false)
    @NotBlank(message = "O endereço é obrigatório.")
    @Schema(example = "Rua das Palmeiras, 100 - Centro")
    private String endereco;

    @Column (name = "hora_abertura_quadra", nullable=false)
    @NotNull(message = "A hora de abertura é obrigatória.")
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", pattern = "HH:mm", example = "08:00")
    private LocalTime horaAbertura;

    @Column (name = "hora_fechamento_quadra", nullable=false)
    @NotNull(message = "A hora de fechamento é obrigatória.")
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", pattern = "HH:mm", example = "23:00")
    private LocalTime horaFechamento;

    @Column (name = "ativa_quadra", nullable=false)
    @Schema(description = "Quadra inativa (manutenção, reforma) não aceita reservas. Padrão: true", example = "true")
    private Boolean ativa;
}
