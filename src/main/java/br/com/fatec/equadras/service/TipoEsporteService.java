package br.com.fatec.equadras.service;

import br.com.fatec.equadras.exception.RecursoNaoEncontradoException;
import br.com.fatec.equadras.exception.RegraNegocioException;
import br.com.fatec.equadras.model.TipoEsporte;
import br.com.fatec.equadras.repository.TipoEsporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class TipoEsporteService {

    // Anotação que indica que injetamos uma dependência. Nesse caso, a Repository com os seus métodos
    @Autowired
    private TipoEsporteRepository tipoEsporteRepository;

    public TipoEsporte cadastrarTipoEsporte(TipoEsporte tipoEsporte){
        tipoEsporte.setId_tipo_esporte(null); // o ID é sempre gerado pelo banco
        tipoEsporte.setCodigo(normalizarCodigo(tipoEsporte.getCodigo()));
        tipoEsporte.setNome(tipoEsporte.getNome().trim());
        if (tipoEsporte.getAtivo() == null) {
            tipoEsporte.setAtivo(true); // todo esporte novo já começa disponível
        }

        // Regra: o código precisa ficar só com letras, números e _ (é o que as aplicações enviam no JSON)
        if (!tipoEsporte.getCodigo().matches("[A-Z0-9_]{3,30}")) {
            throw new RegraNegocioException("Código inválido: use de 3 a 30 letras, números ou _ (ex.: FUTEVOLEI, BEACH_TENNIS).");
        }
        // Regra: dois esportes não podem ter o mesmo código
        if (tipoEsporteRepository.findByCodigo(tipoEsporte.getCodigo()).isPresent()) {
            throw RegraNegocioException.conflito("Já existe um tipo de esporte com o código " + tipoEsporte.getCodigo() + ".");
        }
        return tipoEsporteRepository.save(tipoEsporte);
    }

    public List<TipoEsporte> listarTiposEsporte() {
        return tipoEsporteRepository.findAll(Sort.by("nome"));
    }

    public TipoEsporte pesquisarTipoEsporte (Long id){
        return tipoEsporteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de esporte não encontrado. ID: " + id));
    }

    public TipoEsporte pesquisarTipoEsportePorCodigo (String codigo) {
        return tipoEsporteRepository.findByCodigo(normalizarCodigo(codigo))
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tipo de esporte não encontrado: " + codigo + ". Consulte os disponíveis em GET /tipo-esporte."));
    }

    public TipoEsporte ativarTipoEsporte (Long id) {
        return alterarSituacao(id, true);
    }

    public TipoEsporte inativarTipoEsporte (Long id) {
        return alterarSituacao(id, false);
    }

    // Regra: só muda a situação se ela for diferente da atual (ativar o que já está ativo é recusado)
    private TipoEsporte alterarSituacao(Long id, boolean ativo) {
        TipoEsporte tipoEsporte = pesquisarTipoEsporte(id);
        if (tipoEsporte.getAtivo() == ativo) {
            throw new RegraNegocioException("O esporte " + tipoEsporte.getNome() + " já está " + (ativo ? "ativo" : "inativo") + ".");
        }
        tipoEsporte.setAtivo(ativo);
        return tipoEsporteRepository.save(tipoEsporte);
    }

    // "futevôlei" -> "FUTEVOLEI" | "beach tennis" -> "BEACH_TENNIS"
    private String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(codigo.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase().replaceAll("[\\s-]+", "_");
    }
}
