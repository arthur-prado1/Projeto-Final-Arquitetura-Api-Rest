package br.com.fatec.equadras.controller;

import br.com.fatec.equadras.config.ExemplosApi;
import br.com.fatec.equadras.exception.ErroResposta;
import br.com.fatec.equadras.model.TipoEsporte;
import br.com.fatec.equadras.service.TipoEsporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipo-esporte")
@Tag(name = "Tipos de Esporte")
public class TipoEsporteController {

    @Autowired
    private TipoEsporteService tipoEsporteService;

    @Operation(summary = "Cadastrar tipo de esporte",
            description = "Cria uma nova modalidade para as quadras. O código é gravado em MAIÚSCULAS, sem acento e com _ no lugar de espaço, e não pode se repetir. Se `ativo` não for enviado, o esporte nasce ativo.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExemplosApi.TIPO_ESPORTE_REQUISICAO)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tipo de esporte cadastrado",
                    content = @Content(schema = @Schema(implementation = TipoEsporte.class), examples = @ExampleObject(value = ExemplosApi.TIPO_ESPORTE_RESPOSTA))),
            @ApiResponse(responseCode = "400", description = "Campos inválidos ou código fora do padrão",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe esporte com este código",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoEsporte cadastrarTipoEsporte(@Valid @RequestBody TipoEsporte tipoEsporte){
        return tipoEsporteService.cadastrarTipoEsporte(tipoEsporte);
    }

    @Operation(summary = "Listar tipos de esporte",
            description = "Ativos e inativos, ordenados por nome. O `codigo` é o valor usado no campo `tipoEsporte` das quadras.")
    @ApiResponse(responseCode = "200", description = "Lista de tipos de esporte",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TipoEsporte.class)),
                    examples = @ExampleObject(value = ExemplosApi.TIPOS_ESPORTE_LISTA)))
    @GetMapping
    public List<TipoEsporte> listarTiposEsporte(){
        return tipoEsporteService.listarTiposEsporte();
    }

    @Operation(summary = "Ativar tipo de esporte pelo ID", description = "Volta a permitir o uso do esporte em novas quadras.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Esporte ativado", content = @Content(schema = @Schema(implementation = TipoEsporte.class))),
            @ApiResponse(responseCode = "400", description = "Esporte já está ativo", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de esporte não encontrado", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/ativar/{id}")
    public TipoEsporte ativarTipoEsporte(@Parameter(description = "ID do tipo de esporte", example = "1") @PathVariable Long id){
        return tipoEsporteService.ativarTipoEsporte(id);
    }

    @Operation(summary = "Inativar tipo de esporte pelo ID",
            description = "O esporte deixa de poder ser usado em novas quadras. As quadras que já existem continuam funcionando normalmente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Esporte inativado", content = @Content(schema = @Schema(implementation = TipoEsporte.class))),
            @ApiResponse(responseCode = "400", description = "Esporte já está inativo", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de esporte não encontrado", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/inativar/{id}")
    public TipoEsporte inativarTipoEsporte(@Parameter(description = "ID do tipo de esporte", example = "1") @PathVariable Long id){
        return tipoEsporteService.inativarTipoEsporte(id);
    }

}
