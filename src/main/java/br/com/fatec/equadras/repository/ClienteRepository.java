package br.com.fatec.equadras.repository;

import br.com.fatec.equadras.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Query Method
    Optional<Cliente> findByCpf (String cpf);

    Optional<Cliente> findByEmailIgnoreCase (String email);

    List<Cliente> findByNomeContainingIgnoreCaseOrderByNomeAsc (String nome);

}
