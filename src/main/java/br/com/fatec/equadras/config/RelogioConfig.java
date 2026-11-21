package br.com.fatec.equadras.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelogioConfig {

    // Relógio oficial da API no fuso de Brasília. Os services pedem a hora atual a ele,
    // o que permite aos testes "congelar" a data/hora e testar regras como "não reservar no passado".
    @Bean
    public Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
