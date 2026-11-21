package br.com.fatec.equadras.service;

import br.com.fatec.equadras.exception.RecursoNaoEncontradoException;
import br.com.fatec.equadras.exception.RegraNegocioException;
import br.com.fatec.equadras.model.Cliente;
import br.com.fatec.equadras.repository.ClienteRepository;
import br.com.fatec.equadras.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    // Anotação que indica que injetamos uma dependência. Nesse caso, a Repository com os seus métodos
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    public Cliente cadastrarCliente(Cliente cliente){
        cliente.setId_cliente(null); // o ID é sempre gerado pelo banco
        normalizar(cliente);
        validarDuplicidade(cliente, null);
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll(Sort.by("nome"));
    }

    public Cliente pesquisarCliente (Long id){
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado. ID: " + id));
    }

    public Cliente pesquisarClientePorCpf (String cpf) {
        return clienteRepository.findByCpf(somenteDigitos(cpf))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum cliente com o CPF " + cpf + "."));
    }

    public List<Cliente> pesquisarClientePorParteDoNome (String nome) {
        return clienteRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome);
    }

    public Cliente atualizarCliente (Long id, Cliente clienteAtualizado) {
        Cliente cliente = pesquisarCliente(id);

        normalizar(clienteAtualizado);
        validarDuplicidade(clienteAtualizado, id);

        cliente.setNome(clienteAtualizado.getNome());
        cliente.setCpf(clienteAtualizado.getCpf());
        cliente.setEmail(clienteAtualizado.getEmail());
        cliente.setTelefone(clienteAtualizado.getTelefone());

        return clienteRepository.save(cliente);
    }

    public void deletarCliente (Long id) {
        Cliente cliente = pesquisarCliente(id);

        // Regra: não apaga cliente com histórico de reservas (as reservas ficariam sem dono)
        if (reservaRepository.countByCliente(cliente) > 0) {
            throw RegraNegocioException.conflito("Cliente possui reservas registradas e não pode ser excluído.");
        }
        clienteRepository.delete(cliente);
    }

    // Regra: CPF e e-mail não podem se repetir entre clientes
    private void validarDuplicidade(Cliente cliente, Long idAtual) {
        Optional<Cliente> mesmoCpf = clienteRepository.findByCpf(cliente.getCpf());
        if (mesmoCpf.isPresent() && !mesmoCpf.get().getId_cliente().equals(idAtual)) {
            throw RegraNegocioException.conflito("Já existe um cliente cadastrado com este CPF.");
        }
        Optional<Cliente> mesmoEmail = clienteRepository.findByEmailIgnoreCase(cliente.getEmail());
        if (mesmoEmail.isPresent() && !mesmoEmail.get().getId_cliente().equals(idAtual)) {
            throw RegraNegocioException.conflito("Já existe um cliente cadastrado com este e-mail.");
        }
    }

    // Guarda CPF só com números e e-mail em minúsculas: "529.982.247-25" e "52998224725" são o mesmo CPF
    private void normalizar(Cliente cliente) {
        cliente.setCpf(somenteDigitos(cliente.getCpf()));
        if (cliente.getEmail() != null) {
            cliente.setEmail(cliente.getEmail().trim().toLowerCase());
        }
        if (cliente.getNome() != null) {
            cliente.setNome(cliente.getNome().trim());
        }
    }

    private String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}
