package br.com.fatec.equadras.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Descrição geral da API exibida no topo do Swagger (http://localhost:8080/swagger-ui.html)
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacaoEquadras() {
        return new OpenAPI()
                .info(new Info()
                        .title("eQuadras API")
                        .version("1.0.0")
                        .description("""
                                API REST para **agendamento de quadras esportivas** (futebol society, futsal, vôlei, beach tennis, tênis, basquete e padel).

                                **Problema:** donos de quadras controlam horários em cadernos e conversas de WhatsApp, o que gera \
                                reservas duplicadas no mesmo horário, cancelamentos em cima da hora e falta de controle do faturamento.

                                **Solução:** um serviço central que qualquer aplicação (site, app, bot de WhatsApp, sistema da recepção) \
                                pode consumir para cadastrar clientes e quadras, consultar horários livres e reservar com segurança.

                                **Principais regras de negócio**
                                - Impede duas reservas na mesma quadra no mesmo horário (e o mesmo cliente em dois lugares ao mesmo tempo).
                                - Só reserva dentro do horário de funcionamento, em horas cheias e nunca no passado.
                                - Quadra inativa não aceita reservas.
                                - Valor total calculado automaticamente (valor da hora x horas).
                                - Cancelamento e remarcação só com 2 horas de antecedência.
                                - Cliente e quadra com histórico de reservas não podem ser excluídos.

                                **Formato:** JSON. Datas `yyyy-MM-dd`, horas `HH:mm`. Todos os erros seguem o modelo `ErroResposta`.""")
                        .contact(new Contact().name("Equipe eQuadras").url("https://github.com/arthur-prado1"))
                        .license(new License().name("MIT")))
                .tags(List.of(
                        new Tag().name("Clientes").description("Cadastro das pessoas que alugam as quadras"),
                        new Tag().name("Quadras").description("Cadastro das quadras, esporte, preço e horário de funcionamento"),
                        new Tag().name("Reservas").description("Agendamento, remarcação, cancelamento, grade de horários e faturamento")));
    }
}
