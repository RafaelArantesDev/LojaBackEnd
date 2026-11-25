package br.com.loja.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Confere com o próprio Supabase se o token enviado pelo React é válido
 * (GET {SUPABASE_URL}/auth/v1/user). Resultados válidos ficam em cache por 60 s
 * para não chamar o Supabase a cada requisição.
 */
@Service
public class SupabaseTokenService {

    private static final long VALIDADE_CACHE_MS = 60_000;
    private static final int LIMITE_CACHE = 1000;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Map<String, Long> cache = new ConcurrentHashMap<>();
    private final String url;
    private final String chave;

    public SupabaseTokenService(
            @Value("${app.supabase.url:}") String url,
            @Value("${app.supabase.anon-key:}") String chave) {

        this.url = url.trim().replaceAll("/+$", "");
        this.chave = chave.trim();
    }

    public boolean valido(String token) {
        if (token == null || token.isBlank() || url.isEmpty() || chave.isEmpty()) {
            return false;
        }

        long agora = System.currentTimeMillis();
        Long validoAte = cache.get(token);

        if (validoAte != null && validoAte > agora) {
            return true;
        }

        try {
            HttpRequest requisicao = HttpRequest.newBuilder(URI.create(url + "/auth/v1/user"))
                    .timeout(Duration.ofSeconds(5))
                    .header("apikey", chave)
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<Void> resposta = http.send(requisicao, HttpResponse.BodyHandlers.discarding());

            if (resposta.statusCode() == 200) {
                if (cache.size() >= LIMITE_CACHE) {
                    cache.clear();
                }
                cache.put(token, agora + VALIDADE_CACHE_MS);
                return true;
            }
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return false;
    }
}
