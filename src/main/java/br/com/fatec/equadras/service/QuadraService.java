package br.com.fatec.equadras.service;

import br.com.fatec.equadras.exception.RecursoNaoEncontradoException;
import br.com.fatec.equadras.exception.RegraNegocioException;
import br.com.fatec.equadras.model.EsporteDisponivel;
import br.com.fatec.equadras.model.Quadra;
import br.com.fatec.equadras.model.TipoEsporte;
import br.com.fatec.equadras.repository.QuadraRepository;
import br.com.fatec.equadras.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class QuadraService {

    // Anotação que indica que injetamos uma dependência. Nesse caso, a Repository com os seus métodos
    @Autowired
    private QuadraRepository quadraRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    public Quadra cadastrarQuadra(Quadra quadra){
        quadra.setId_quadra(null); // o ID é sempre gerado pelo banco
        quadra.setNome(quadra.getNome().trim());
        if (quadra.getAtiva() == null) {
            quadra.setAtiva(true); // toda quadra nova começa disponível
        }
        validarHorarioFuncionamento(quadra.getHoraAbertura(), quadra.getHoraFechamento());
        validarNomeUnico(quadra.getNome(), null);
        return quadraRepository.save(quadra);
    }

    public List<Quadra> listarQuadras() {
        return quadraRepository.findAll(Sort.by("nome"));
    }

    public Quadra pesquisarQuadra (Long id){
        return quadraRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quadra não encontrada. ID: " + id));
    }

    public Quadra pesquisarQuadraPorNome (String nome) {
        return quadraRepository.findByNomeIgnoreCase(nome.trim())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma quadra com o nome '" + nome + "'."));
    }

    public List<Quadra> pesquisarQuadraPorParteDoNome (String nome) {
        return quadraRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome);
    }

    public List<Quadra> pesquisarQuadraPorEsporte (TipoEsporte tipoEsporte) {
        return quadraRepository.findByTipoEsporteOrderByNomeAsc(tipoEsporte);
    }

    public List<Quadra> listarQuadrasAtivas () {
        return quadraRepository.findByAtivaTrueOrderByNomeAsc();
    }

    // Lista os esportes aceitos, lida direto do enum: um esporte novo no enum aparece aqui automaticamente
    public List<EsporteDisponivel> listarEsportes () {
        return Arrays.stream(TipoEsporte.values())
                .map(esporte -> new EsporteDisponivel(esporte.name(), esporte.getNome()))
                .toList();
    }

    public Quadra atualizarQuadra (Long id, Quadra quadraAtualizada) {
        Quadra quadra = pesquisarQuadra(id);

        validarHorarioFuncionamento(quadraAtualizada.getHoraAbertura(), quadraAtualizada.getHoraFechamento());
        validarNomeUnico(quadraAtualizada.getNome().trim(), id);

        quadra.setNome(quadraAtualizada.getNome().trim());
        quadra.setTipoEsporte(quadraAtualizada.getTipoEsporte());
        quadra.setValorHora(quadraAtualizada.getValorHora());
        quadra.setEndereco(quadraAtualizada.getEndereco());
        quadra.setHoraAbertura(quadraAtualizada.getHoraAbertura());
        quadra.setHoraFechamento(quadraAtualizada.getHoraFechamento());
        if (quadraAtualizada.getAtiva() != null) {
            quadra.setAtiva(quadraAtualizada.getAtiva());
        }

        return quadraRepository.save(quadra);
    }

    public void deletarQuadra (Long id) {
        Quadra quadra = pesquisarQuadra(id);

        // Regra: quadra com histórico de reservas não é apagada (perderia o histórico e o faturamento)
        if (reservaRepository.countByQuadra(quadra) > 0) {
            throw RegraNegocioException.conflito(
                    "Quadra possui reservas registradas e não pode ser excluída. Inative-a (\"ativa\": false) pelo PUT /quadra/" + id + ".");
        }
        quadraRepository.delete(quadra);
    }

    // Regra: a quadra abre e fecha em horas cheias e precisa ficar aberta pelo menos 1 hora
    private void validarHorarioFuncionamento(LocalTime abertura, LocalTime fechamento) {
        if (abertura.getMinute() != 0 || fechamento.getMinute() != 0) {
            throw new RegraNegocioException("Abertura e fechamento devem ser em horas cheias (ex.: 08:00 e 23:00).");
        }
        if (!fechamento.isAfter(abertura)) {
            throw new RegraNegocioException("A hora de fechamento deve ser depois da hora de abertura.");
        }
    }

    // Regra: duas quadras não podem ter o mesmo nome (a busca /quadra/nome/{nome} devolve uma só)
    private void validarNomeUnico(String nome, Long idAtual) {
        Optional<Quadra> mesmoNome = quadraRepository.findByNomeIgnoreCase(nome);
        if (mesmoNome.isPresent() && !mesmoNome.get().getId_quadra().equals(idAtual)) {
            throw RegraNegocioException.conflito("Já existe uma quadra com o nome '" + nome + "'.");
        }
    }
}
