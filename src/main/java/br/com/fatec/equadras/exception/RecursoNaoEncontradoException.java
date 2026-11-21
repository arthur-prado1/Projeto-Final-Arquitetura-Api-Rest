package br.com.fatec.equadras.exception;

// Lançada quando o ID/valor pesquisado não existe no banco -> HTTP 404
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
