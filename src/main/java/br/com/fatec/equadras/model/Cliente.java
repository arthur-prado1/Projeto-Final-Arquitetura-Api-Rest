package br.com.fatec.equadras.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;

@Data
@Entity
@Table(name="cliente")
@Valid // @Valid: usado para implementar todas as outras validações
@JsonPropertyOrder({"id_cliente", "nome", "cpf", "email", "telefone"})
@Schema(description = "Pessoa que aluga as quadras")
public class Cliente {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Schema(description = "Gerado automaticamente pelo banco (ignorado no cadastro)", example = "1")
    private Long id_cliente;

    @Column (name = "nome_cliente", nullable=false)
    @NotBlank(message = "O nome é obrigatório.")
    @Size (min = 3, max = 80, message = "Nome deve ter entre 3 e 80 caracteres.")
    @Schema(example = "Arthur Prado")
    private String nome;

    @Column (name = "cpf_cliente", nullable=false, unique=true)
    @NotBlank(message = "O CPF é obrigatório.")
    @CPF (message = "CPF inválido.")
    @Schema(description = "CPF válido, somente números ou com máscara", example = "52998224725")
    private String cpf;

    @Column (name = "email_cliente", nullable=false, unique=true)
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email (message = "E-mail inválido (email@email.com)")
    @Schema(example = "arthur@email.com")
    private String email;

    @Column (name = "telefone_cliente", nullable=false)
    @NotBlank(message = "O telefone é obrigatório.")
    @Pattern (regexp = "\\d{10,11}", message = "Telefone deve ter DDD + número, somente dígitos (10 ou 11).")
    @Schema(example = "11987654321")
    private String telefone;
}
