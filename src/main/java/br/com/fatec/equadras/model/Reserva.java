package br.com.fatec.equadras.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name="reserva")
@Valid // @Valid: usado para implementar todas as outras validações
@JsonPropertyOrder({"id_reserva", "cliente", "quadra", "data", "horaInicio", "horaFim", "valorTotal", "status", "dataCriacao"})
@Schema(description = "Aluguel de uma quadra por um cliente em uma data e faixa de horário")
public class Reserva {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Schema(description = "Gerado automaticamente pelo banco (ignorado no cadastro)", example = "1")
    private Long id_reserva;

    // ManyToOne: muitas reservas pertencem a um mesmo cliente (chave estrangeira id_cliente)
    @ManyToOne
    @JoinColumn (name = "id_cliente", nullable=false)
    @NotNull(message = "O cliente é obrigatório.")
    @Schema(description = "Basta enviar o ID: {\"id_cliente\": 1}")
    private Cliente cliente;

    // ManyToOne: muitas reservas pertencem a uma mesma quadra (chave estrangeira id_quadra)
    @ManyToOne
    @JoinColumn (name = "id_quadra", nullable=false)
    @NotNull(message = "A quadra é obrigatória.")
    @Schema(description = "Basta enviar o ID: {\"id_quadra\": 1}")
    private Quadra quadra;

    @Column (name = "data_reserva", nullable=false)
    @NotNull(message = "A data é obrigatória.")
    @Schema(type = "string", format = "date", example = "2026-11-30")
    private LocalDate data;

    @Column (name = "hora_inicio_reserva", nullable=false)
    @NotNull(message = "A hora de início é obrigatória.")
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", pattern = "HH:mm", description = "Hora cheia", example = "19:00")
    private LocalTime horaInicio;

    @Column (name = "hora_fim_reserva", nullable=false)
    @NotNull(message = "A hora de término é obrigatória.")
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", pattern = "HH:mm", description = "Hora cheia", example = "21:00")
    private LocalTime horaFim;

    // READ_ONLY: aparece na resposta, mas é ignorado se vier no JSON (calculado pela API)
    @Column (name = "valor_total_reserva", nullable=false, precision = 10, scale = 2)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Calculado automaticamente: valor da hora x quantidade de horas", accessMode = Schema.AccessMode.READ_ONLY, example = "300.00")
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column (name = "status_reserva", nullable=false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Definido pela API: nasce CONFIRMADA e pode ser CANCELADA", accessMode = Schema.AccessMode.READ_ONLY, example = "CONFIRMADA")
    private StatusReserva status;

    @Column (name = "data_criacao_reserva", nullable=false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(type = "string", accessMode = Schema.AccessMode.READ_ONLY, example = "2026-11-21T14:30:00")
    private LocalDateTime dataCriacao;
}
