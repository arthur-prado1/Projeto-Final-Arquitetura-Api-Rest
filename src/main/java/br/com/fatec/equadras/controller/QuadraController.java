package br.com.fatec.equadras.controller;

import br.com.fatec.equadras.config.ExemplosApi;
import br.com.fatec.equadras.exception.ErroResposta;
import br.com.fatec.equadras.model.Quadra;
import br.com.fatec.equadras.model.TipoEsporte;
import br.com.fatec.equadras.service.QuadraService;
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
@RequestMapping("/quadra")
@Tag(name = "Quadras")
public class QuadraController {

    @Autowired
    private QuadraService quadraService;

    @Operation(summary = "Cadastrar quadra",
            description = "Nome único. Abertura e fechamento em horas cheias, com fechamento depois da abertura. Se `ativa` não for enviado, a quadra nasce ativa.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExemplosApi.QUADRA_REQUISICAO)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quadra cadastrada",
                    content = @Content(schema = @Schema(implementation = Quadra.class), examples = @ExampleObject(value = ExemplosApi.QUADRA_RESPOSTA))),
            @ApiResponse(responseCode = "400", description = "Campos inválidos ou horário de funcionamento inválido",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe quadra com este nome",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Quadra cadastrarQuadra(@Valid @RequestBody Quadra quadra){
        return quadraService.cadastrarQuadra(quadra);
    }

    @Operation(summary = "Listar todas as quadras", description = "Ativas e inativas, ordenadas por nome.")
    @ApiResponse(responseCode = "200", description = "Lista de quadras",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Quadra.class))))
    @GetMapping
    public List<Quadra> listarQuadras(){
        return quadraService.listarQuadras();
    }

    @Operation(summary = "Listar quadras ativas", description = "Somente as quadras que aceitam reservas.")
    @ApiResponse(responseCode = "200", description = "Lista de quadras ativas",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Quadra.class))))
    @GetMapping("/ativas")
    public List<Quadra> listarQuadrasAtivas(){
        return quadraService.listarQuadrasAtivas();
    }

    // PathVariable: converta o valor inserido na URL e armazene na variável ID
    @Operation(summary = "Pesquisar quadra pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quadra encontrada",
                    content = @Content(schema = @Schema(implementation = Quadra.class), examples = @ExampleObject(value = ExemplosApi.QUADRA_RESPOSTA))),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_NAO_ENCONTRADO)))
    })
    @GetMapping("/id/{id}")
    public Quadra pesquisarQuadra(@Parameter(description = "ID da quadra", example = "1") @PathVariable Long id){
        return quadraService.pesquisarQuadra(id);
    }

    @Operation(summary = "Pesquisar quadra pelo nome exato", description = "Não diferencia maiúsculas de minúsculas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quadra encontrada", content = @Content(schema = @Schema(implementation = Quadra.class))),
            @ApiResponse(responseCode = "404", description = "Nenhuma quadra com este nome", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/nome/{nome}")
    public Quadra pesquisarQuadraPorNome(@Parameter(description = "Nome completo", example = "Arena Society 1") @PathVariable String nome){
        return quadraService.pesquisarQuadraPorNome(nome);
    }

    @Operation(summary = "Pesquisar quadras por parte do nome")
    @ApiResponse(responseCode = "200", description = "Quadras encontradas (pode ser vazia)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Quadra.class))))
    @GetMapping("/partenome/{nome}")
    public List<Quadra> pesquisarQuadraPorParteDoNome(@Parameter(description = "Trecho do nome", example = "arena") @PathVariable String nome){
        return quadraService.pesquisarQuadraPorParteDoNome(nome);
    }

    @Operation(summary = "Pesquisar quadras por esporte",
            description = "Valores: FUTEBOL_SOCIETY, FUTSAL, VOLEI, BEACH_TENNIS, TENIS, BASQUETE, PADEL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quadras do esporte (pode ser vazia)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Quadra.class)))),
            @ApiResponse(responseCode = "400", description = "Esporte inexistente", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/esporte/{tipoEsporte}")
    public List<Quadra> pesquisarQuadraPorEsporte(@Parameter(description = "Tipo de esporte", example = "BEACH_TENNIS") @PathVariable TipoEsporte tipoEsporte){
        return quadraService.pesquisarQuadraPorEsporte(tipoEsporte);
    }

    @Operation(summary = "Atualizar quadra pelo ID",
            description = "Substitui os dados da quadra. Use `\"ativa\": false` para inativar (manutenção/reforma) sem apagar o histórico.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExemplosApi.QUADRA_REQUISICAO)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quadra atualizada", content = @Content(schema = @Schema(implementation = Quadra.class))),
            @ApiResponse(responseCode = "400", description = "Campos inválidos", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Nome já usado por outra quadra", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/{id}")
    public Quadra atualizarQuadra(@Parameter(description = "ID da quadra", example = "1") @PathVariable Long id, @Valid @RequestBody Quadra quadra){
        return quadraService.atualizarQuadra(id, quadra);
    }

    @Operation(summary = "Deletar quadra pelo ID", description = "Recusado se a quadra tiver reservas registradas (nesse caso, inative-a).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Quadra excluída"),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Quadra possui reservas",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_EXCLUSAO_COM_RESERVAS)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarQuadra(@Parameter(description = "ID da quadra", example = "1") @PathVariable Long id){
        quadraService.deletarQuadra(id);
    }

}
