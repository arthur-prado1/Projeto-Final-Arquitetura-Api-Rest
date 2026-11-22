package br.com.fatec.equadras.repository;

import br.com.fatec.equadras.model.TipoEsporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoEsporteRepository extends JpaRepository<TipoEsporte, Long> {

    // Query Method
    Optional<TipoEsporte> findByCodigo (String codigo);

}
