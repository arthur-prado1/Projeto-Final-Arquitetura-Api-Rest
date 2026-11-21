package br.com.fatec.equadras.config;

import br.com.fatec.equadras.model.*;
import br.com.fatec.equadras.repository.ClienteRepository;
import br.com.fatec.equadras.repository.QuadraRepository;
import br.com.fatec.equadras.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Cadastra dados de exemplo quando o banco está vazio, para a API já ter o que mostrar na primeira execução.
// Desligue com equadras.carga-inicial=false no application.properties.
@Component
@ConditionalOnProperty(name = "equadras.carga-inicial", havingValue = "true")
public class CargaInicialDados implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialDados.class);

    @Autowired
    private QuadraRepository quadraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private Clock relogio;

    @Override
    public void run(String... args) {
        if (quadraRepository.count() > 0 || clienteRepository.count() > 0) {
            return; // banco já tem dados: não mexe em nada
        }

        Quadra society = quadraRepository.save(quadra("Arena Society 1", TipoEsporte.FUTEBOL_SOCIETY, "150.00", "Rua das Palmeiras, 100 - Centro", 8, 23));
        quadraRepository.save(quadra("Quadra Beach Tennis Sol", TipoEsporte.BEACH_TENNIS, "90.00", "Av. Beira Rio, 2500 - Jardim América", 7, 22));
        quadraRepository.save(quadra("Ginásio Futsal Central", TipoEsporte.FUTSAL, "120.00", "Rua XV de Novembro, 45 - Centro", 9, 23));

        Cliente arthur = clienteRepository.save(cliente("Arthur Prado", "52998224725", "arthur@email.com", "11987654321"));
        clienteRepository.save(cliente("Marina Souza", "11144477735", "marina@email.com", "11912345678"));

        // Uma reserva amanhã das 19h às 21h para a grade de horários já mostrar um horário ocupado
        Reserva reserva = new Reserva();
        reserva.setCliente(arthur);
        reserva.setQuadra(society);
        reserva.setData(LocalDate.now(relogio).plusDays(1));
        reserva.setHoraInicio(LocalTime.of(19, 0));
        reserva.setHoraFim(LocalTime.of(21, 0));
        reserva.setValorTotal(new BigDecimal("300.00"));
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setDataCriacao(LocalDateTime.now(relogio).withNano(0));
        reservaRepository.save(reserva);

        log.info("Carga inicial: 3 quadras, 2 clientes e 1 reserva de exemplo cadastrados.");
    }

    private Quadra quadra(String nome, TipoEsporte esporte, String valorHora, String endereco, int abre, int fecha) {
        Quadra q = new Quadra();
        q.setNome(nome);
        q.setTipoEsporte(esporte);
        q.setValorHora(new BigDecimal(valorHora));
        q.setEndereco(endereco);
        q.setHoraAbertura(LocalTime.of(abre, 0));
        q.setHoraFechamento(LocalTime.of(fecha, 0));
        q.setAtiva(true);
        return q;
    }

    private Cliente cliente(String nome, String cpf, String email, String telefone) {
        Cliente c = new Cliente();
        c.setNome(nome);
        c.setCpf(cpf);
        c.setEmail(email);
        c.setTelefone(telefone);
        return c;
    }
}
