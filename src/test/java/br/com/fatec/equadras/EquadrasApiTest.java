package br.com.fatec.equadras;

import br.com.fatec.equadras.model.Cliente;
import br.com.fatec.equadras.model.Quadra;
import br.com.fatec.equadras.model.TipoEsporte;
import br.com.fatec.equadras.repository.ClienteRepository;
import br.com.fatec.equadras.repository.QuadraRepository;
import br.com.fatec.equadras.repository.ReservaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testa a API de ponta a ponta (requisição HTTP -> controller -> service -> banco H2) com o relógio fixo em 10/01/2030 10:00
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RelogioFixoConfig.class)
class EquadrasApiTest {

    private static final String DIA_FUTURO = "2030-01-15";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private QuadraRepository quadraRepository;

    private Long idCliente;
    private Long idOutroCliente;
    private Long idQuadra;
    private Long idOutraQuadra;

    @BeforeEach
    void prepararBanco() {
        reservaRepository.deleteAll();
        clienteRepository.deleteAll();
        quadraRepository.deleteAll();

        idQuadra = quadraRepository.save(quadra("Arena Society 1", TipoEsporte.FUTEBOL_SOCIETY, "150.00", true)).getId_quadra();
        idOutraQuadra = quadraRepository.save(quadra("Quadra Beach 1", TipoEsporte.BEACH_TENNIS, "90.00", true)).getId_quadra();
        idCliente = clienteRepository.save(cliente("Arthur Prado", "52998224725", "arthur@email.com")).getId_cliente();
        idOutroCliente = clienteRepository.save(cliente("Marina Souza", "11144477735", "marina@email.com")).getId_cliente();
    }

    // ===================== RESERVAS: REGRAS DE NEGÓCIO =====================

    @Test
    @DisplayName("Cria a reserva confirmada e calcula o valor total automaticamente")
    void criaReservaECalculaValor() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "21:00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.valorTotal").value(300.0))
                .andExpect(jsonPath("$.cliente.nome").value("Arthur Prado"))
                .andExpect(jsonPath("$.quadra.nome").value("Arena Society 1"))
                .andExpect(jsonPath("$.dataCriacao").value("2030-01-10T10:00:00"));
    }

    @Test
    @DisplayName("Impede duas reservas na mesma quadra no mesmo horário")
    void impedeConflitoNaMesmaQuadra() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "21:00").andExpect(status().isCreated());

        reservar(idOutroCliente, idQuadra, DIA_FUTURO, "20:00", "22:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Horário indisponível")));
    }

    @Test
    @DisplayName("Permite reservas encostadas (uma termina às 21h e a outra começa às 21h)")
    void permiteReservasEncostadas() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "21:00").andExpect(status().isCreated());
        reservar(idOutroCliente, idQuadra, DIA_FUTURO, "21:00", "22:00").andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Impede o mesmo cliente de ter duas reservas ao mesmo tempo em quadras diferentes")
    void impedeClienteEmDuasQuadrasAoMesmoTempo() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "21:00").andExpect(status().isCreated());

        reservar(idCliente, idOutraQuadra, DIA_FUTURO, "20:00", "21:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("O cliente já tem uma reserva")));
    }

    @Test
    @DisplayName("Recusa reserva fora do horário de funcionamento da quadra")
    void recusaForaDoFuncionamento() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "06:00", "08:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("Fora do horário de funcionamento")));
    }

    @Test
    @DisplayName("Recusa reserva em hora quebrada")
    void recusaHoraQuebrada() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:30", "21:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("horas cheias")));
    }

    @Test
    @DisplayName("Recusa reserva com término antes do início")
    void recusaTerminoAntesDoInicio() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "21:00", "19:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("término deve ser depois")));
    }

    @Test
    @DisplayName("Recusa reserva no passado")
    void recusaReservaNoPassado() throws Exception {
        reservar(idCliente, idQuadra, "2030-01-05", "19:00", "20:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("já passou")));
    }

    @Test
    @DisplayName("Recusa reserva em quadra inativa")
    void recusaQuadraInativa() throws Exception {
        Long idInativa = quadraRepository.save(quadra("Quadra em Reforma", TipoEsporte.FUTSAL, "100.00", false)).getId_quadra();

        reservar(idCliente, idInativa, DIA_FUTURO, "19:00", "20:00")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("inativa")));
    }

    @Test
    @DisplayName("Devolve 404 quando o cliente ou a quadra da reserva não existem")
    void reservaComQuadraInexistente() throws Exception {
        reservar(idCliente, 999L, DIA_FUTURO, "19:00", "20:00").andExpect(status().isNotFound());
        reservar(999L, idQuadra, DIA_FUTURO, "19:00", "20:00").andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Ignora valorTotal e status enviados pelo cliente da API")
    void ignoraCamposCalculados() throws Exception {
        String json = """
                {"cliente":{"id_cliente":%d},"quadra":{"id_quadra":%d},"data":"%s","horaInicio":"19:00","horaFim":"20:00",
                 "valorTotal":1.00,"status":"CANCELADA"}""".formatted(idCliente, idQuadra, DIA_FUTURO);

        mockMvc.perform(post("/reserva").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(150.0))
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("Cancelar libera o horário para outra reserva")
    void cancelarLiberaHorario() throws Exception {
        Long idReserva = idDe(reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "21:00"), "$.id_reserva");

        mockMvc.perform(put("/reserva/cancelar/" + idReserva))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        reservar(idOutroCliente, idQuadra, DIA_FUTURO, "19:00", "21:00").andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Não cancela com menos de 2 horas de antecedência")
    void naoCancelaEmCimaDaHora() throws Exception {
        // relógio dos testes = 10/01/2030 10:00 -> jogo às 11:00 começa em 1 hora
        Long idReserva = idDe(reservar(idCliente, idQuadra, "2030-01-10", "11:00", "12:00"), "$.id_reserva");

        mockMvc.perform(put("/reserva/cancelar/" + idReserva))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("2 horas de antecedência")));
    }

    @Test
    @DisplayName("Não cancela uma reserva já cancelada")
    void naoCancelaDuasVezes() throws Exception {
        Long idReserva = idDe(reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "20:00"), "$.id_reserva");
        mockMvc.perform(put("/reserva/cancelar/" + idReserva)).andExpect(status().isOk());

        mockMvc.perform(put("/reserva/cancelar/" + idReserva))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("já está cancelada")));
    }

    @Test
    @DisplayName("Remarcar revalida as regras e recalcula o valor")
    void remarcarRecalculaValor() throws Exception {
        Long idReserva = idDe(reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "20:00"), "$.id_reserva");

        mockMvc.perform(put("/reserva/" + idReserva).contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(idCliente, idQuadra, DIA_FUTURO, "19:00", "22:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaFim").value("22:00"))
                .andExpect(jsonPath("$.valorTotal").value(450.0));
    }

    @Test
    @DisplayName("Remarcar para um horário ocupado é recusado")
    void remarcarParaHorarioOcupado() throws Exception {
        reservar(idOutroCliente, idQuadra, DIA_FUTURO, "21:00", "22:00").andExpect(status().isCreated());
        Long idReserva = idDe(reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "20:00"), "$.id_reserva");

        mockMvc.perform(put("/reserva/" + idReserva).contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson(idCliente, idQuadra, DIA_FUTURO, "20:00", "22:00")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Reserva confirmada só pode ser excluída depois de cancelada")
    void excluirReserva() throws Exception {
        Long idReserva = idDe(reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "20:00"), "$.id_reserva");

        mockMvc.perform(delete("/reserva/" + idReserva)).andExpect(status().isConflict());
        mockMvc.perform(put("/reserva/cancelar/" + idReserva)).andExpect(status().isOk());
        mockMvc.perform(delete("/reserva/" + idReserva)).andExpect(status().isNoContent());
        mockMvc.perform(get("/reserva/id/" + idReserva)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Grade de horários mostra o que está livre e o que está reservado")
    void gradeDeHorarios() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "21:00").andExpect(status().isCreated());

        // quadra abre às 08:00 -> índice 0 = 08:00, índice 11 = 19:00
        mockMvc.perform(get("/reserva/disponibilidade/" + idQuadra + "/" + DIA_FUTURO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(15))
                .andExpect(jsonPath("$[10].horaInicio").value("18:00"))
                .andExpect(jsonPath("$[10].disponivel").value(true))
                .andExpect(jsonPath("$[11].situacao").value("Reservado"))
                .andExpect(jsonPath("$[12].situacao").value("Reservado"))
                .andExpect(jsonPath("$[13].disponivel").value(true));
    }

    @Test
    @DisplayName("Faturamento soma apenas as reservas confirmadas")
    void faturamento() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "21:00").andExpect(status().isCreated());
        reservar(idOutroCliente, idQuadra, DIA_FUTURO, "21:00", "22:00").andExpect(status().isCreated());
        Long idCancelada = idDe(reservar(idCliente, idQuadra, "2030-01-16", "10:00", "11:00"), "$.id_reserva");
        mockMvc.perform(put("/reserva/cancelar/" + idCancelada)).andExpect(status().isOk());

        mockMvc.perform(get("/reserva/faturamento/" + idQuadra))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservasConfirmadas").value(2))
                .andExpect(jsonPath("$.reservasCanceladas").value(1))
                .andExpect(jsonPath("$.horasVendidas").value(3))
                .andExpect(jsonPath("$.faturamentoTotal").value(450.0));
    }

    // ===================== QUADRAS E CLIENTES =====================

    @Test
    @DisplayName("Quadra com reservas não pode ser excluída")
    void naoExcluiQuadraComReservas() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "20:00").andExpect(status().isCreated());

        mockMvc.perform(delete("/quadra/" + idQuadra))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("Inative-a")));
    }

    @Test
    @DisplayName("Quadra sem reservas pode ser excluída")
    void excluiQuadraSemReservas() throws Exception {
        mockMvc.perform(delete("/quadra/" + idOutraQuadra)).andExpect(status().isNoContent());
        mockMvc.perform(get("/quadra/id/" + idOutraQuadra)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cadastra quadra ativa por padrão e recusa horário de funcionamento inválido")
    void cadastroDeQuadra() throws Exception {
        String valida = """
                {"nome":"Quadra Padel 1","tipoEsporte":"PADEL","valorHora":110.00,"endereco":"Rua A, 1",
                 "horaAbertura":"07:00","horaFechamento":"22:00"}""";
        mockMvc.perform(post("/quadra").contentType(MediaType.APPLICATION_JSON).content(valida))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativa").value(true))
                .andExpect(jsonPath("$.horaAbertura").value("07:00"));

        String invertida = valida.replace("Quadra Padel 1", "Quadra Padel 2").replace("\"07:00\"", "\"23:00\"");
        mockMvc.perform(post("/quadra").contentType(MediaType.APPLICATION_JSON).content(invertida))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/quadra").contentType(MediaType.APPLICATION_JSON).content(valida))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Pesquisas de quadra por esporte e valor de esporte inválido")
    void pesquisasDeQuadra() throws Exception {
        mockMvc.perform(get("/quadra/esporte/BEACH_TENNIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Quadra Beach 1"));
        mockMvc.perform(get("/quadra/esporte/XADREZ")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/quadra/partenome/arena")).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/quadra/nome/arena society 1")).andExpect(status().isOk());
        mockMvc.perform(get("/quadra/id/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Valida os campos do cliente e devolve a lista de erros")
    void validaCamposDoCliente() throws Exception {
        String invalido = """
                {"nome":"Al","cpf":"12345678900","email":"sem-arroba","telefone":"123"}""";

        mockMvc.perform(post("/cliente").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(4));
    }

    @Test
    @DisplayName("Não aceita CPF repetido, mesmo enviado com máscara")
    void naoAceitaCpfRepetido() throws Exception {
        String repetido = """
                {"nome":"Outra Pessoa","cpf":"529.982.247-25","email":"outra@email.com","telefone":"11999998888"}""";

        mockMvc.perform(post("/cliente").contentType(MediaType.APPLICATION_JSON).content(repetido))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("CPF")));
    }

    @Test
    @DisplayName("CRUD de cliente: cadastrar, pesquisar, atualizar e excluir")
    void crudDeCliente() throws Exception {
        String novo = """
                {"nome":"Joao Silva","cpf":"123.456.789-09","email":"JOAO@Email.com","telefone":"11955554444"}""";
        Long id = idDe(mockMvc.perform(post("/cliente").contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("12345678909"))
                .andExpect(jsonPath("$.email").value("joao@email.com")), "$.id_cliente");

        mockMvc.perform(get("/cliente/cpf/123.456.789-09")).andExpect(jsonPath("$.id_cliente").value(id));
        mockMvc.perform(get("/cliente/partenome/joao")).andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/cliente/" + id).contentType(MediaType.APPLICATION_JSON).content(novo.replace("Joao Silva", "Joao Pedro Silva")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Pedro Silva"));

        mockMvc.perform(delete("/cliente/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/cliente/id/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cliente com reservas não pode ser excluído")
    void naoExcluiClienteComReservas() throws Exception {
        reservar(idCliente, idQuadra, DIA_FUTURO, "19:00", "20:00").andExpect(status().isCreated());
        mockMvc.perform(delete("/cliente/" + idCliente)).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Documentação OpenAPI é gerada")
    void documentacaoGerada() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("eQuadras API"));
    }

    // ===================== AUXILIARES =====================

    private ResultActions reservar(Long cliente, Long quadra, String data, String inicio, String fim) throws Exception {
        return mockMvc.perform(post("/reserva").contentType(MediaType.APPLICATION_JSON)
                .content(reservaJson(cliente, quadra, data, inicio, fim)));
    }

    private String reservaJson(Long cliente, Long quadra, String data, String inicio, String fim) {
        return """
                {"cliente":{"id_cliente":%d},"quadra":{"id_quadra":%d},"data":"%s","horaInicio":"%s","horaFim":"%s"}"""
                .formatted(cliente, quadra, data, inicio, fim);
    }

    private Long idDe(ResultActions resposta, String caminho) throws Exception {
        String corpo = resposta.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, caminho)).longValue();
    }

    private Quadra quadra(String nome, TipoEsporte esporte, String valorHora, boolean ativa) {
        Quadra q = new Quadra();
        q.setNome(nome);
        q.setTipoEsporte(esporte);
        q.setValorHora(new BigDecimal(valorHora));
        q.setEndereco("Rua Teste, 10");
        q.setHoraAbertura(LocalTime.of(8, 0));
        q.setHoraFechamento(LocalTime.of(23, 0));
        q.setAtiva(ativa);
        return q;
    }

    private Cliente cliente(String nome, String cpf, String email) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setCpf(cpf);
        c.setEmail(email);
        c.setTelefone("11987654321");
        return c;
    }
}
