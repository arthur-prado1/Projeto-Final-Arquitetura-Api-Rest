package br.com.fatec.equadras.controller;

import br.com.fatec.equadras.config.ExemplosApi;
import br.com.fatec.equadras.exception.ErroResposta;
import br.com.fatec.equadras.model.HorarioDisponivel;
import br.com.fatec.equadras.model.Reserva;
import br.com.fatec.equadras.model.ResumoFaturamento;
import br.com.fatec.equadras.model.StatusReserva;
import br.com.fatec.equadras.service.ReservaService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reserva")
@Tag(name = "Reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Operation(summary = "Cadastrar reserva",
            description = """
                    Informe só o ID do cliente e da quadra. A API aplica as regras:
                    quadra ativa; horas cheias; não reservar no passado; dentro do horário de funcionamento;
                    sem conflito com outra reserva da quadra nem com outra reserva do mesmo cliente.
                    `valorTotal`, `status` e `dataCriacao` são preenchidos automaticamente.""")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExemplosApi.RESERVA_REQUISICAO)))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva confirmada",
                    content = @Content(schema = @Schema(implementation = Reserva.class), examples = @ExampleObject(value = ExemplosApi.RESERVA_RESPOSTA))),
            @ApiResponse(responseCode = "400", description = "Campos inválidos ou regra violada (passado, fora do funcionamento, quadra inativa, hora quebrada)",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_FORA_FUNCIONAMENTO))),
            @ApiResponse(responseCode = "404", description = "Cliente ou quadra não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Horário já reservado (quadra ou cliente)",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_HORARIO_OCUPADO)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva cadastrarReserva(@Valid @RequestBody Reserva reserva){
        return reservaService.cadastrarReserva(reserva);
    }

    @Operation(summary = "Listar todas as reservas", description = "Ordenadas por data e hora de início.")
    @ApiResponse(responseCode = "200", description = "Lista de reservas",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Reserva.class))))
    @GetMapping
    public List<Reserva> listarReservas(){
        return reservaService.listarReservas();
    }

    // PathVariable: converta o valor inserido na URL e armazene na variável ID
    @Operation(summary = "Pesquisar reserva pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                    content = @Content(schema = @Schema(implementation = Reserva.class), examples = @ExampleObject(value = ExemplosApi.RESERVA_RESPOSTA))),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_NAO_ENCONTRADO)))
    })
    @GetMapping("/id/{id}")
    public Reserva pesquisarReserva(@Parameter(description = "ID da reserva", example = "1") @PathVariable Long id){
        return reservaService.pesquisarReserva(id);
    }

    @Operation(summary = "Pesquisar reservas de um cliente", description = "Histórico completo do cliente (confirmadas e canceladas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas do cliente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Reserva.class)))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/cliente/{idCliente}")
    public List<Reserva> pesquisarReservaPorCliente(@Parameter(description = "ID do cliente", example = "1") @PathVariable Long idCliente){
        return reservaService.pesquisarReservaPorCliente(idCliente);
    }

    @Operation(summary = "Pesquisar reservas de uma quadra", description = "Agenda completa da quadra (confirmadas e canceladas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas da quadra",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Reserva.class)))),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/quadra/{idQuadra}")
    public List<Reserva> pesquisarReservaPorQuadra(@Parameter(description = "ID da quadra", example = "1") @PathVariable Long idQuadra){
        return reservaService.pesquisarReservaPorQuadra(idQuadra);
    }

    @Operation(summary = "Pesquisar reservas de uma data", description = "Agenda do dia de todas as quadras. Formato yyyy-MM-dd.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas do dia",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Reserva.class)))),
            @ApiResponse(responseCode = "400", description = "Data em formato inválido", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/data/{data}")
    public List<Reserva> pesquisarReservaPorData(
            @Parameter(description = "Data (yyyy-MM-dd)", example = "2026-11-30") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data){
        return reservaService.pesquisarReservaPorData(data);
    }

    @Operation(summary = "Pesquisar reservas por status", description = "Valores: CONFIRMADA ou CANCELADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservas com o status",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Reserva.class)))),
            @ApiResponse(responseCode = "400", description = "Status inexistente", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/status/{status}")
    public List<Reserva> pesquisarReservaPorStatus(@Parameter(description = "Status", example = "CONFIRMADA") @PathVariable StatusReserva status){
        return reservaService.pesquisarReservaPorStatus(status);
    }

    @Operation(summary = "Consultar horários livres de uma quadra em uma data",
            description = "Devolve a grade do dia em faixas de 1 hora, do horário de abertura ao de fechamento, indicando o que está livre.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grade do dia",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HorarioDisponivel.class)),
                            examples = @ExampleObject(value = ExemplosApi.DISPONIBILIDADE_RESPOSTA))),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/disponibilidade/{idQuadra}/{data}")
    public List<HorarioDisponivel> consultarDisponibilidade(
            @Parameter(description = "ID da quadra", example = "1") @PathVariable Long idQuadra,
            @Parameter(description = "Data (yyyy-MM-dd)", example = "2026-11-30") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data){
        return reservaService.consultarDisponibilidade(idQuadra, data);
    }

    @Operation(summary = "Calcular faturamento de uma quadra",
            description = "Soma automaticamente o valor das reservas confirmadas e mostra horas vendidas e cancelamentos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo do faturamento",
                    content = @Content(schema = @Schema(implementation = ResumoFaturamento.class), examples = @ExampleObject(value = ExemplosApi.FATURAMENTO_RESPOSTA))),
            @ApiResponse(responseCode = "404", description = "Quadra não encontrada", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @GetMapping("/faturamento/{idQuadra}")
    public ResumoFaturamento calcularFaturamento(@Parameter(description = "ID da quadra", example = "1") @PathVariable Long idQuadra){
        return reservaService.calcularFaturamento(idQuadra);
    }

    @Operation(summary = "Atualizar (remarcar) reserva pelo ID",
            description = "Troca data, horário, quadra ou cliente revalidando todas as regras. Só para reservas confirmadas e com 2 horas de antecedência. O valor é recalculado.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = ExemplosApi.RESERVA_REQUISICAO)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva remarcada", content = @Content(schema = @Schema(implementation = Reserva.class))),
            @ApiResponse(responseCode = "400", description = "Regra violada (cancelada, antecedência, horário inválido)", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Reserva, cliente ou quadra não encontrado", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Novo horário já reservado", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/{id}")
    public Reserva atualizarReserva(@Parameter(description = "ID da reserva", example = "1") @PathVariable Long id, @Valid @RequestBody Reserva reserva){
        return reservaService.atualizarReserva(id, reserva);
    }

    @Operation(summary = "Cancelar reserva pelo ID",
            description = "Muda o status para CANCELADA e libera o horário. Só com pelo menos 2 horas de antecedência do início do jogo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada", content = @Content(schema = @Schema(implementation = Reserva.class))),
            @ApiResponse(responseCode = "400", description = "Já cancelada ou fora do prazo de cancelamento",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class), examples = @ExampleObject(value = ExemplosApi.ERRO_ANTECEDENCIA))),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @PutMapping("/cancelar/{id}")
    public Reserva cancelarReserva(@Parameter(description = "ID da reserva", example = "1") @PathVariable Long id){
        return reservaService.cancelarReserva(id);
    }

    @Operation(summary = "Deletar reserva pelo ID", description = "Só reservas CANCELADAS podem ser excluídas (as confirmadas precisam ser canceladas antes).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reserva excluída"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada", content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Reserva ainda confirmada", content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarReserva(@Parameter(description = "ID da reserva", example = "1") @PathVariable Long id){
        reservaService.deletarReserva(id);
    }

}
