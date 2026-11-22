package br.com.fatec.equadras.config;

// Exemplos de JSON exibidos na documentação Swagger (requisições e respostas)
public final class ExemplosApi {

    private ExemplosApi() {
    }

    // ---------- CLIENTE ----------
    public static final String CLIENTE_REQUISICAO = """
            {
              "nome": "Arthur Prado",
              "cpf": "52998224725",
              "email": "arthur@email.com",
              "telefone": "11987654321"
            }""";

    public static final String CLIENTE_RESPOSTA = """
            {
              "id_cliente": 1,
              "nome": "Arthur Prado",
              "cpf": "52998224725",
              "email": "arthur@email.com",
              "telefone": "11987654321"
            }""";

    // ---------- QUADRA ----------
    public static final String QUADRA_REQUISICAO = """
            {
              "nome": "Arena Society 1",
              "tipoEsporte": "FUTEBOL_SOCIETY",
              "valorHora": 150.00,
              "endereco": "Rua das Palmeiras, 100 - Centro",
              "horaAbertura": "08:00",
              "horaFechamento": "23:00",
              "ativa": true
            }""";

    public static final String QUADRA_RESPOSTA = """
            {
              "id_quadra": 1,
              "nome": "Arena Society 1",
              "tipoEsporte": "FUTEBOL_SOCIETY",
              "valorHora": 150.00,
              "endereco": "Rua das Palmeiras, 100 - Centro",
              "horaAbertura": "08:00",
              "horaFechamento": "23:00",
              "ativa": true
            }""";

    public static final String ESPORTES_RESPOSTA = """
            [
              { "codigo": "FUTEBOL_SOCIETY", "nome": "Futebol Society" },
              { "codigo": "FUTSAL", "nome": "Futsal" },
              { "codigo": "VOLEI", "nome": "Vôlei" },
              { "codigo": "BEACH_TENNIS", "nome": "Beach Tennis" },
              { "codigo": "TENIS", "nome": "Tênis" },
              { "codigo": "BASQUETE", "nome": "Basquete" },
              { "codigo": "PADEL", "nome": "Padel" }
            ]""";

    // ---------- RESERVA ----------
    public static final String RESERVA_REQUISICAO = """
            {
              "cliente": { "id_cliente": 1 },
              "quadra": { "id_quadra": 1 },
              "data": "2026-11-30",
              "horaInicio": "19:00",
              "horaFim": "21:00"
            }""";

    public static final String RESERVA_RESPOSTA = """
            {
              "id_reserva": 1,
              "cliente": {
                "id_cliente": 1,
                "nome": "Arthur Prado",
                "cpf": "52998224725",
                "email": "arthur@email.com",
                "telefone": "11987654321"
              },
              "quadra": {
                "id_quadra": 1,
                "nome": "Arena Society 1",
                "tipoEsporte": "FUTEBOL_SOCIETY",
                "valorHora": 150.00,
                "endereco": "Rua das Palmeiras, 100 - Centro",
                "horaAbertura": "08:00",
                "horaFechamento": "23:00",
                "ativa": true
              },
              "data": "2026-11-30",
              "horaInicio": "19:00",
              "horaFim": "21:00",
              "valorTotal": 300.00,
              "status": "CONFIRMADA",
              "dataCriacao": "2026-11-21T14:30:00"
            }""";

    public static final String DISPONIBILIDADE_RESPOSTA = """
            [
              { "horaInicio": "18:00", "horaFim": "19:00", "disponivel": true,  "situacao": "Livre" },
              { "horaInicio": "19:00", "horaFim": "20:00", "disponivel": false, "situacao": "Reservado" },
              { "horaInicio": "20:00", "horaFim": "21:00", "disponivel": false, "situacao": "Reservado" },
              { "horaInicio": "21:00", "horaFim": "22:00", "disponivel": true,  "situacao": "Livre" }
            ]""";

    public static final String FATURAMENTO_RESPOSTA = """
            {
              "idQuadra": 1,
              "quadra": "Arena Society 1",
              "reservasConfirmadas": 4,
              "reservasCanceladas": 1,
              "horasVendidas": 7,
              "faturamentoTotal": 1050.00
            }""";

    // ---------- ERROS ----------
    public static final String ERRO_VALIDACAO = """
            {
              "dataHora": "2026-11-21T14:30:00",
              "status": 400,
              "erro": "Bad Request",
              "mensagem": "Dados inválidos. Corrija os campos indicados.",
              "caminho": "/cliente",
              "campos": [
                { "campo": "cpf", "mensagem": "CPF inválido." },
                { "campo": "email", "mensagem": "E-mail inválido (email@email.com)" }
              ]
            }""";

    public static final String ERRO_NAO_ENCONTRADO = """
            {
              "dataHora": "2026-11-21T14:30:00",
              "status": 404,
              "erro": "Not Found",
              "mensagem": "Registro não encontrado. ID: 99",
              "caminho": "/.../id/99"
            }""";

    public static final String ERRO_HORARIO_OCUPADO = """
            {
              "dataHora": "2026-11-21T14:30:00",
              "status": 409,
              "erro": "Conflict",
              "mensagem": "Horário indisponível: a quadra já está reservada das 19:00 às 21:00 nesta data.",
              "caminho": "/reserva"
            }""";

    public static final String ERRO_FORA_FUNCIONAMENTO = """
            {
              "dataHora": "2026-11-21T14:30:00",
              "status": 400,
              "erro": "Bad Request",
              "mensagem": "Fora do horário de funcionamento: a quadra 'Arena Society 1' funciona das 08:00 às 23:00.",
              "caminho": "/reserva"
            }""";

    public static final String ERRO_ANTECEDENCIA = """
            {
              "dataHora": "2026-11-21T14:30:00",
              "status": 400,
              "erro": "Bad Request",
              "mensagem": "A reserva só pode ser cancelada com pelo menos 2 horas de antecedência do início do jogo.",
              "caminho": "/reserva/cancelar/1"
            }""";

    public static final String ERRO_DUPLICADO = """
            {
              "dataHora": "2026-11-21T14:30:00",
              "status": 409,
              "erro": "Conflict",
              "mensagem": "Já existe um cliente cadastrado com este CPF.",
              "caminho": "/cliente"
            }""";

    public static final String ERRO_EXCLUSAO_COM_RESERVAS = """
            {
              "dataHora": "2026-11-21T14:30:00",
              "status": 409,
              "erro": "Conflict",
              "mensagem": "Quadra possui reservas registradas e não pode ser excluída. Inative-a (\\"ativa\\": false) pelo PUT /quadra/1.",
              "caminho": "/quadra/1"
            }""";
}
