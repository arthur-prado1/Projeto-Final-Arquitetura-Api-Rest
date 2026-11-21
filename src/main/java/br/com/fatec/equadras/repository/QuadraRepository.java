package br.com.fatec.equadras.repository;

import br.com.fatec.equadras.model.Quadra;
import br.com.fatec.equadras.model.TipoEsporte;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuadraRepository extends JpaRepository<Quadra, Long> {

    // Query Method
    Optional<Quadra> findByNomeIgnoreCase (String nome);

    List<Quadra> findByNomeContainingIgnoreCaseOrderByNomeAsc (String nome);

    List<Quadra> findByTipoEsporteOrderByNomeAsc (TipoEsporte tipoEsporte);

    List<Quadra> findByAtivaTrueOrderByNomeAsc ();

    // Lock pessimista (SELECT ... FOR UPDATE): enquanto uma reserva está sendo criada nesta quadra,
    // outra requisição para a MESMA quadra espera a primeira terminar. Evita duas reservas no mesmo horário
    // quando dois clientes clicam ao mesmo tempo.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quadra q where q.id_quadra = :id")
    Optional<Quadra> buscarComTrava (@Param("id") Long id);

}
