package com.br.sojogabr.infrastructure.security;

import com.br.sojogabr.application.port.out.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    // CORREÇÃO: Adicionados todos os caminhos públicos definidos no SecurityConfig
    private final List<String> publicPaths = List.of(
            "/api/login",
            "/api/users",
            "/actuator",
            "/",
            "/index.html",
            "/cadastro.html",
            "/static/",
            "/favicon.ico"
    );

    public JwtAuthenticationFilter(TokenProvider tokenProvider, @Qualifier("userService") UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Impede que o filtro seja executado para os endpoints públicos definidos.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        // Retorna true (não filtra) se o caminho da requisição for exatamente igual a um dos caminhos
        // ou se começar com um dos caminhos que representam diretórios (como /static/ ou /actuator/).
        return publicPaths.stream().anyMatch(path ->
                requestPath.equals(path) || (path.endsWith("/") && requestPath.startsWith(path))
        );
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = tokenProvider.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (tokenProvider.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}