package br.com.fatec.equadras.exception;

import org.springframework.http.HttpStatus;

// Lançada quando uma regra de negócio é violada.
// 400 (BAD_REQUEST) = pedido inválido para a regra | 409 (CONFLICT) = conflito com dados já existentes
public class RegraNegocioException extends RuntimeException {

    private final HttpStatus status;

    public RegraNegocioException(String mensagem) {
        this(HttpStatus.BAD_REQUEST, mensagem);
    }

    public RegraNegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static RegraNegocioException conflito(String mensagem) {
        return new RegraNegocioException(HttpStatus.CONFLICT, mensagem);
    }
}
