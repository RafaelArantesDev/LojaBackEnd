package br.com.loja.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Devolve todos os erros no formato {"mensagem": "..."} para o React exibir. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> tratarStatus(ResponseStatusException e) {
        String mensagem = e.getReason() != null ? e.getReason() : "Erro ao processar a requisição.";
        return ResponseEntity.status(e.getStatusCode()).body(corpo(mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(corpo("Dados inválidos. Confira os campos informados."));
    }

    private Map<String, String> corpo(String mensagem) {
        Map<String, String> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}
