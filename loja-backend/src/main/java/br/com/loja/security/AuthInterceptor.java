package br.com.loja.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * Quando app.auth.ativa=true, exige "Authorization: Bearer <token do Supabase>"
 * nas rotas /api/**. Com app.auth.ativa=false (padrão) a API fica aberta,
 * como no backend das aulas.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SupabaseTokenService tokenService;
    private final boolean autenticacaoAtiva;

    public AuthInterceptor(
            SupabaseTokenService tokenService,
            @Value("${app.auth.ativa:false}") boolean autenticacaoAtiva) {

        this.tokenService = tokenService;
        this.autenticacaoAtiva = autenticacaoAtiva;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!autenticacaoAtiva) {
            return true;
        }

        // Pré-voo do CORS (OPTIONS) não carrega token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && tokenService.valido(header.substring(7).trim())) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"mensagem\":\"Sessão expirada ou não autenticada.\"}");
        return false;
    }
}
