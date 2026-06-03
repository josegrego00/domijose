package com.josepinodev.appdomirest.config;

import com.josepinodev.appdomirest.model.UserEntity;
import com.josepinodev.appdomirest.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class UserActiveFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public UserActiveFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final RequestMatcher publicPaths = new OrRequestMatcher(
            AntPathRequestMatcher.antMatcher("/"),
            AntPathRequestMatcher.antMatcher("/catalogo"),
            AntPathRequestMatcher.antMatcher("/producto/**"),
            AntPathRequestMatcher.antMatcher("/buscar"),
            AntPathRequestMatcher.antMatcher("/login"),
            AntPathRequestMatcher.antMatcher("/register"),
            AntPathRequestMatcher.antMatcher("/css/**"),
            AntPathRequestMatcher.antMatcher("/js/**"),
            AntPathRequestMatcher.antMatcher("/images/**"),
            AntPathRequestMatcher.antMatcher("/imagenes/**"),
            AntPathRequestMatcher.antMatcher("/sounds/**"),
            AntPathRequestMatcher.antMatcher("/ws/**")
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (publicPaths.matches(request)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }

        String username = auth.getName();

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty() || !userOpt.get().getActivo()) {
            log.warn("Usuario inactivo o no encontrado, cerrando sesion: {}", username);
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();
            response.sendRedirect("/login?blocked");
            return;
        }

        chain.doFilter(request, response);
    }
}
