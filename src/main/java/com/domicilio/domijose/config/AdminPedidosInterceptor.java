package com.domicilio.domijose.config;

import com.domicilio.domijose.models.enums.OrderStatus;
import com.domicilio.domijose.repositories.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AdminPedidosInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminPedidosInterceptor.class);
    private final OrderRepository orderRepository;

    public AdminPedidosInterceptor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && isAdmin(authentication)) {
                long pedidosPendientes = orderRepository.countByStatus(OrderStatus.POR_CONFIRMAR);
                modelAndView.addObject("pedidosPendientesAdmin", pedidosPendientes);
                log.debug("Pedidos pendientes para admin: {}", pedidosPendientes);
            }
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}