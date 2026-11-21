package br.com.fatec.equadras.service;

import br.com.fatec.equadras.exception.RecursoNaoEncontradoException;
import br.com.fatec.equadras.exception.RegraNegocioException;
import br.com.fatec.equadras.model.*;
import br.com.fatec.equadras.repository.ClienteRepository;
import br.com.fatec.equadras.repository.QuadraRepository;
import br.com.fatec.equadras.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    // Antecedência mínima para cancelar ou remarcar uma reserva
    public static final int ANTECEDENCIA_MINIMA_HORAS = 2;

    // Anotação que indica que injetamos uma dependência. Nesse caso, as Repositories com os seus métodos
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private QuadraRepository quadraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Relógio da API (fuso de Brasília), definido em config/RelogioConfig
    @Autowired
    private Clock relogio;

    // @Transactional: tudo dentro do método acontece em uma única transação no banco.
    // A trava da quadra (lock pessimista) só é liberada no final, depois que a reserva foi gravada.
    @Transactional
    public Reserva cadastrarReserva(Reserva reserva){
        Quadra quadra = buscarQuadraComTrava(reserva.getQuadra());
        Cliente cliente = buscarCliente(reserva.getCliente());

        reserva.setId_reserva(null); // o ID é sempre gerado pelo banco
        reserva.setQuadra(quadra);
        reserva.setCliente(cliente);

        validarReserva(reserva, null);

        reserva.setValorTotal(calcularValorTotal(quadra, reserva.getHoraInicio(), reserva.getHoraFim()));
        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva.setDataCriacao(LocalDateTime.now(relogio).withNano(0));

        return reservaRepository.save(reserva);
    }

    public List<Reserva> listarReservas() {
        return reservaRepository.findAll(Sort.by("data", "horaInicio"));
    }

    public Reserva pesquisarReserva (Long id){
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada. ID: " + id));
    }

    public List<Reserva> pesquisarReservaPorCliente (Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado. ID: " + idCliente));
        return reservaRepository.findByClienteOrderByDataAscHoraInicioAsc(cliente);
    }

    public List<Reserva> pesquisarReservaPorQuadra (Long idQuadra) {
        Quadra quadra = quadraRepository.findById(idQuadra)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quadra não encontrada. ID: " + idQuadra));
        return reservaRepository.findByQuadraOrderByDataAscHoraInicioAsc(quadra);
    }

    public List<Reserva> pesquisarReservaPorData (LocalDate data) {
        return reservaRepository.findByDataOrderByHoraInicioAsc(data);
    }

    public List<Reserva> pesquisarReservaPorStatus (StatusReserva status) {
        return reservaRepository.findByStatusOrderByDataAscHoraInicioAsc(status);
    }

    // Remarcação: troca data/horário (e, se quiser, a quadra) revalidando todas as regras
    @Transactional
    public Reserva atualizarReserva (Long id, Reserva reservaAtualizada) {
        Reserva reserva = pesquisarReserva(id);

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new RegraNegocioException("Reserva cancelada não pode ser alterada. Faça uma nova reserva.");
        }
        validarAntecedencia(reserva, "remarcada");

        Quadra quadra = buscarQuadraComTrava(reservaAtualizada.getQuadra());
        Cliente cliente = buscarCliente(reservaAtualizada.getCliente());

        reserva.setQuadra(quadra);
        reserva.setCliente(cliente);
        reserva.setData(reservaAtualizada.getData());
        reserva.setHoraInicio(reservaAtualizada.getHoraInicio());
        reserva.setHoraFim(reservaAtualizada.getHoraFim());

        validarReserva(reserva, id);
        reserva.setValorTotal(calcularValorTotal(quadra, reserva.getHoraInicio(), reserva.getHoraFim()));

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva cancelarReserva (Long id) {
        Reserva reserva = pesquisarReserva(id);

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new RegraNegocioException("Esta reserva já está cancelada.");
        }
        validarAntecedencia(reserva, "cancelada");

        // Reserva cancelada libera o horário: as checagens de conflito só olham reservas CONFIRMADAS
        reserva.setStatus(StatusReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    public void deletarReserva (Long id) {
        Reserva reserva = pesquisarReserva(id);

        // Regra: reserva confirmada não some do sistema sem antes ser cancelada (preserva o histórico)
        if (reserva.getStatus() == StatusReserva.CONFIRMADA) {
            throw RegraNegocioException.conflito("Reserva confirmada não pode ser excluída. Cancele antes pelo PUT /reserva/cancelar/" + id + ".");
        }
        reservaRepository.delete(reserva);
    }

    // Grade do dia: uma linha por hora de funcionamento dizendo se está livre ou não
    public List<HorarioDisponivel> consultarDisponibilidade (Long idQuadra, LocalDate data) {
        Quadra quadra = quadraRepository.findById(idQuadra)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quadra não encontrada. ID: " + idQuadra));

        List<Reserva> reservasDoDia = reservaRepository.findByQuadraAndDataAndStatus(quadra, data, StatusReserva.CONFIRMADA);
        LocalDateTime agora = LocalDateTime.now(relogio);
        List<HorarioDisponivel> grade = new ArrayList<>();

        LocalTime inicio = quadra.getHoraAbertura();
        while (inicio.isBefore(quadra.getHoraFechamento())) {
            LocalTime fim = inicio.plusHours(1);
            final LocalTime inicioSlot = inicio;

            String situacao = "Livre";
            if (!quadra.getAtiva()) {
                situacao = "Quadra inativa";
            } else if (!LocalDateTime.of(data, inicioSlot).isAfter(agora)) {
                situacao = "Horário já passou";
            } else if (reservasDoDia.stream().anyMatch(r -> horariosSeSobrepoem(r.getHoraInicio(), r.getHoraFim(), inicioSlot, fim))) {
                situacao = "Reservado";
            }

            grade.add(new HorarioDisponivel(inicioSlot, fim, situacao.equals("Livre"), situacao));
            inicio = fim;
        }
        return grade;
    }

    // Faturamento calculado a partir das reservas confirmadas da quadra
    public ResumoFaturamento calcularFaturamento (Long idQuadra) {
        Quadra quadra = quadraRepository.findById(idQuadra)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quadra não encontrada. ID: " + idQuadra));

        List<Reserva> confirmadas = reservaRepository.findByQuadraAndStatus(quadra, StatusReserva.CONFIRMADA);
        long canceladas = reservaRepository.countByQuadraAndStatus(quadra, StatusReserva.CANCELADA);

        BigDecimal total = BigDecimal.ZERO;
        long horas = 0;
        for (Reserva r : confirmadas) {
            total = total.add(r.getValorTotal());
            horas += Duration.between(r.getHoraInicio(), r.getHoraFim()).toHours();
        }
        return new ResumoFaturamento(quadra.getId_quadra(), quadra.getNome(), confirmadas.size(), canceladas, horas, total);
    }

    // ================= REGRAS DE NEGÓCIO DA RESERVA =================

    private void validarReserva(Reserva reserva, Long idIgnorado) {
        Quadra quadra = reserva.getQuadra();
        LocalTime inicio = reserva.getHoraInicio();
        LocalTime fim = reserva.getHoraFim();

        // RN01 - Quadra inativa (manutenção, reforma) não aceita reservas
        if (!quadra.getAtiva()) {
            throw new RegraNegocioException("A quadra '" + quadra.getNome() + "' está inativa e não aceita reservas.");
        }

        // RN02 - Reservas em horas cheias, com término depois do início (mínimo de 1 hora)
        if (inicio.getMinute() != 0 || fim.getMinute() != 0) {
            throw new RegraNegocioException("As reservas são feitas em horas cheias (ex.: 19:00 às 21:00).");
        }
        if (!fim.isAfter(inicio)) {
            throw new RegraNegocioException("A hora de término deve ser depois da hora de início (mínimo de 1 hora).");
        }

        // RN03 - Não é possível reservar um horário que já passou
        if (!LocalDateTime.of(reserva.getData(), inicio).isAfter(LocalDateTime.now(relogio))) {
            throw new RegraNegocioException("Não é possível reservar um horário que já passou.");
        }

        // RN04 - A reserva precisa estar dentro do horário de funcionamento da quadra
        if (inicio.isBefore(quadra.getHoraAbertura()) || fim.isAfter(quadra.getHoraFechamento())) {
            throw new RegraNegocioException("Fora do horário de funcionamento: a quadra '" + quadra.getNome()
                    + "' funciona das " + quadra.getHoraAbertura() + " às " + quadra.getHoraFechamento() + ".");
        }

        // RN05 - Impedir duas reservas na mesma quadra no mesmo horário
        List<Reserva> reservasDaQuadra = reservaRepository.findByQuadraAndDataAndStatus(quadra, reserva.getData(), StatusReserva.CONFIRMADA);
        Optional<Reserva> conflitoQuadra = procurarConflito(reservasDaQuadra, inicio, fim, idIgnorado);
        if (conflitoQuadra.isPresent()) {
            throw RegraNegocioException.conflito("Horário indisponível: a quadra já está reservada das "
                    + conflitoQuadra.get().getHoraInicio() + " às " + conflitoQuadra.get().getHoraFim() + " nesta data.");
        }

        // RN06 - O mesmo cliente não pode ter duas reservas ao mesmo tempo (em quadras diferentes)
        List<Reserva> reservasDoCliente = reservaRepository.findByClienteAndDataAndStatus(reserva.getCliente(), reserva.getData(), StatusReserva.CONFIRMADA);
        Optional<Reserva> conflitoCliente = procurarConflito(reservasDoCliente, inicio, fim, idIgnorado);
        if (conflitoCliente.isPresent()) {
            throw RegraNegocioException.conflito("O cliente já tem uma reserva na quadra '"
                    + conflitoCliente.get().getQuadra().getNome() + "' das " + conflitoCliente.get().getHoraInicio()
                    + " às " + conflitoCliente.get().getHoraFim() + " nesta data.");
        }
    }

    // RN07 - Valor total calculado automaticamente: valor da hora x quantidade de horas
    private BigDecimal calcularValorTotal(Quadra quadra, LocalTime inicio, LocalTime fim) {
        long horas = Duration.between(inicio, fim).toHours();
        return quadra.getValorHora().multiply(BigDecimal.valueOf(horas));
    }

    // RN08 - Cancelar ou remarcar só com no mínimo 2 horas de antecedência do início do jogo
    private void validarAntecedencia(Reserva reserva, String acao) {
        LocalDateTime inicioDoJogo = LocalDateTime.of(reserva.getData(), reserva.getHoraInicio());
        LocalDateTime limite = LocalDateTime.now(relogio).plusHours(ANTECEDENCIA_MINIMA_HORAS);
        if (inicioDoJogo.isBefore(limite)) {
            throw new RegraNegocioException("A reserva só pode ser " + acao + " com pelo menos "
                    + ANTECEDENCIA_MINIMA_HORAS + " horas de antecedência do início do jogo.");
        }
    }

    // Dois horários se sobrepõem quando um começa antes do outro terminar (19-21 x 20-22 = conflito; 19-20 x 20-21 = ok)
    private boolean horariosSeSobrepoem(LocalTime inicioA, LocalTime fimA, LocalTime inicioB, LocalTime fimB) {
        return inicioA.isBefore(fimB) && fimA.isAfter(inicioB);
    }

    private Optional<Reserva> procurarConflito(List<Reserva> reservas, LocalTime inicio, LocalTime fim, Long idIgnorado) {
        return reservas.stream()
                .filter(r -> !r.getId_reserva().equals(idIgnorado)) // na remarcação, ignora a própria reserva
                .filter(r -> horariosSeSobrepoem(r.getHoraInicio(), r.getHoraFim(), inicio, fim))
                .findFirst();
    }

    private Quadra buscarQuadraComTrava(Quadra quadra) {
        if (quadra.getId_quadra() == null) {
            throw new RegraNegocioException("Informe a quadra pelo ID: \"quadra\": {\"id_quadra\": 1}");
        }
        return quadraRepository.buscarComTrava(quadra.getId_quadra())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quadra não encontrada. ID: " + quadra.getId_quadra()));
    }

    private Cliente buscarCliente(Cliente cliente) {
        if (cliente.getId_cliente() == null) {
            throw new RegraNegocioException("Informe o cliente pelo ID: \"cliente\": {\"id_cliente\": 1}");
        }
        return clienteRepository.findById(cliente.getId_cliente())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado. ID: " + cliente.getId_cliente()));
    }
}
