package br.com.fatec.equadras;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

// Nos testes, "agora" é sempre 10/01/2030 às 10:00 — assim as regras de data e antecedência dão sempre o mesmo resultado
@TestConfiguration
public class RelogioFixoConfig {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final LocalDateTime AGORA = LocalDateTime.of(2030, 1, 10, 10, 0);

    @Bean
    @Primary
    public Clock relogioTeste() {
        return Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);
    }
}
