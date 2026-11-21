package br.com.fatec.equadras.controller;

import br.com.fatec.equadras.config.ExemplosApi;
import br.com.fatec.equadras.exception.ErroResposta;
import br.com.fatec.equadras.model.Cliente;
import br.com.fatec.equadras.service.ClienteService;
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
@RequestMapping("/cliente")
@Tag(name = "Clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Operation(summary = "Cadastrar cliente", description = "CPF e e-mail não podem se repetir. O CPF é validado e gravado só com números.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExemplosApi.CLIENTE_REQUISICAO)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado",
                    content = @Content(schema = @Schema(implementation = Cliente.class), examples = @ExampleObject(value = ExemplosApi.CLIENTE_RESPOSTA))),
            @ApiResponse(responseCode = "400", description = "Campos inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_DUPLICADO)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente cadastrarCliente(@Valid @RequestBody Cliente cliente){
        return clienteService.cadastrarCliente(cliente);
    }

    @Operation(summary = "Listar todos os clientes", description = "Ordenados por nome.")
    @ApiResponse(responseCode = "200", description = "Lista de clientes (pode ser vazia)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cliente.class))))
    @GetMapping
    public List<Cliente> listarClientes(){
        return clienteService.listarClientes();
    }

    // PathVariable: converta o valor inserido na URL e armazene na variável ID
    @Operation(summary = "Pesquisar cliente pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = Cliente.class), examples = @ExampleObject(value = ExemplosApi.CLIENTE_RESPOSTA))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_NAO_ENCONTRADO)))
    })
    @GetMapping("/id/{id}")
    public Cliente pesquisarCliente(@Parameter(description = "ID do cliente", example = "1") @PathVariable Long id){
        return clienteService.pesquisarCliente(id);
    }

    @Operation(summary = "Pesquisar cliente pelo CPF", description = "Aceita o CPF com ou sem máscara.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado", content = @Content(schema = @Schema(implementation = Cliente.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum cliente com este CPF", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/cpf/{cpf}")
    public Cliente pesquisarClientePorCpf(@Parameter(description = "CPF do cliente", example = "52998224725") @PathVariable String cpf){
        return clienteService.pesquisarClientePorCpf(cpf);
    }

    @Operation(summary = "Pesquisar clientes por parte do nome", description = "Não diferencia maiúsculas de minúsculas.")
    @ApiResponse(responseCode = "200", description = "Clientes encontrados (pode ser vazia)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cliente.class))))
    @GetMapping("/partenome/{nome}")
    public List<Cliente> pesquisarClientePorParteDoNome(@Parameter(description = "Trecho do nome", example = "arth") @PathVariable String nome){
        return clienteService.pesquisarClientePorParteDoNome(nome);
    }

    @Operation(summary = "Atualizar cliente pelo ID", description = "Substitui todos os dados do cliente. CPF e e-mail continuam únicos.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExemplosApi.CLIENTE_REQUISICAO)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado", content = @Content(schema = @Schema(implementation = Cliente.class))),
            @ApiResponse(responseCode = "400", description = "Campos inválidos", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail pertence a outro cliente", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/{id}")
    public Cliente atualizarCliente(@Parameter(description = "ID do cliente", example = "1") @PathVariable Long id, @Valid @RequestBody Cliente cliente){
        return clienteService.atualizarCliente(id, cliente);
    }

    @Operation(summary = "Deletar cliente pelo ID", description = "Recusado se o cliente tiver reservas registradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente excluído"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Cliente possui reservas", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarCliente(@Parameter(description = "ID do cliente", example = "1") @PathVariable Long id){
        clienteService.deletarCliente(id);
    }

}
