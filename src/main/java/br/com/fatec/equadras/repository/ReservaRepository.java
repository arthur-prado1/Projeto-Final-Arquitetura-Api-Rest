package br.com.fatec.equadras.repository;

import br.com.fatec.equadras.model.Cliente;
import br.com.fatec.equadras.model.Quadra;
import br.com.fatec.equadras.model.Reserva;
import br.com.fatec.equadras.model.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Query Method
    List<Reserva> findByClienteOrderByDataAscHoraInicioAsc (Cliente cliente);

    List<Reserva> findByQuadraOrderByDataAscHoraInicioAsc (Quadra quadra);

    List<Reserva> findByDataOrderByHoraInicioAsc (LocalDate data);

    List<Reserva> findByStatusOrderByDataAscHoraInicioAsc (StatusReserva status);

    // Reservas de uma quadra em um dia com determinado status (usado na checagem de conflito e na grade)
    List<Reserva> findByQuadraAndDataAndStatus (Quadra quadra, LocalDate data, StatusReserva status);

    // Reservas de um cliente em um dia com determinado status (cliente não pode estar em dois lugares ao mesmo tempo)
    List<Reserva> findByClienteAndDataAndStatus (Cliente cliente, LocalDate data, StatusReserva status);

    List<Reserva> findByQuadraAndStatus (Quadra quadra, StatusReserva status);

    long countByQuadraAndStatus (Quadra quadra, StatusReserva status);

    // countBy (e não existsBy): o existsBy do Spring Data procura um campo chamado "id",
    // e aqui o identificador segue o padrão id_<entidade> (id_reserva)
    long countByCliente (Cliente cliente);

    long countByQuadra (Quadra quadra);

}
