package br.com.fatec.equadras.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// @RestControllerAdvice: "escuta" as exceções lançadas por qualquer controller e devolve um JSON de erro padronizado
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - campos que não passaram nas validações (@NotBlank, @CPF, @Email, @Positive...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErroResposta.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroResposta.CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos. Corrija os campos indicados.", request, campos);
    }

    // 400 / 409 - regras de negócio
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return montar(ex.getStatus(), ex.getMessage(), request, null);
    }

    // 404 - registro não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    // 400 - JSON mal formado, data/hora em formato errado ou valor de enum inexistente
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "JSON inválido. Confira o formato: datas yyyy-MM-dd, horas HH:mm e o tipo de cada campo (ex.: \"tipoEsporte\": {\"codigo\": \"PADEL\"}).",
                request, null);
    }

    // 400 - valor errado na URL (ex.: /reserva/data/amanha ou /quadra/esporte/XADREZ)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarParametroInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensagem = "Valor inválido para '" + ex.getName() + "': " + ex.getValue() + ".";
        // Parâmetro do tipo enum (esporte, status): lista os valores aceitos
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            mensagem += " Valores aceitos: " + valoresAceitos(ex.getRequiredType()) + ".";
        }
        return montar(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    private String valoresAceitos(Class<?> tipoEnum) {
        return Arrays.stream(tipoEnum.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    // 409 - segurança extra: o banco recusou por chave única/estrangeira
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, "Operação recusada pelo banco de dados: registro duplicado ou vinculado a outro.", request, null);
    }

    // 404 - rota inexistente
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> tratarRotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, "Endpoint não encontrado. Consulte a documentação em /swagger-ui.html.", request, null);
    }

    private ResponseEntity<ErroResposta> montar(HttpStatus status, String mensagem, HttpServletRequest request,
                                                List<ErroResposta.CampoInvalido> campos) {
        ErroResposta corpo = new ErroResposta(LocalDateTime.now().withNano(0), status.value(), status.getReasonPhrase(),
                mensagem, request.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
