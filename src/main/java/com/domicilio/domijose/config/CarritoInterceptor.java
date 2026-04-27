package com.domicilio.domijose.config;

import com.domicilio.domijose.services.CarritoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CarritoInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CarritoInterceptor.class);
    private final CarritoService carritoService;

    public CarritoInterceptor(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                int carritoCount = carritoService.getCantidadItems(session);
                modelAndView.addObject("carritoCount", carritoCount);
                log.debug("Carrito count agregado al modelo: {}", carritoCount);
            }
        }
    }
}