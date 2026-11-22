package br.com.fatec.equadras.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name="tipo_esporte")
@Valid // @Valid: usado para implementar todas as outras validações
@JsonPropertyOrder({"id_tipo_esporte", "codigo", "nome", "ativo"})
@Schema(description = "Modalidade esportiva aceita pela plataforma (futebol society, beach tennis...)")
public class TipoEsporte {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    @Schema(description = "Gerado automaticamente pelo banco (ignorado no cadastro)", example = "1")
    private Long id_tipo_esporte;

    @Column (name = "codigo_tipo_esporte", nullable=false, unique=true)
    @NotBlank(message = "O código é obrigatório.")
    @Size (max = 30, message = "Código deve ter no máximo 30 caracteres.")
    @Schema(description = "Identificador usado pelas aplicações. Gravado em MAIÚSCULAS, sem acento e com _ no lugar de espaço (ex.: \"futevôlei\" vira FUTEVOLEI)",
            example = "FUTEVOLEI")
    private String codigo;

    @Column (name = "nome_tipo_esporte", nullable=false)
    @NotBlank(message = "O nome é obrigatório.")
    @Size (min = 3, max = 40, message = "Nome deve ter entre 3 e 40 caracteres.")
    @Schema(description = "Nome para exibir nas telas", example = "Futevôlei")
    private String nome;

    @Column (name = "ativo_tipo_esporte", nullable=false)
    @Schema(description = "Esporte inativo não pode ser usado em novas quadras. Padrão: true", example = "true")
    private Boolean ativo;
}
