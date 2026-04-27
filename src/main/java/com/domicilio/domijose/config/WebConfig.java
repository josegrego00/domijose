package com.domicilio.domijose.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC para registrar interceptores a nivel global.
 * WebMvcConfigurer es la interfaz principal que permite personalizar
 * la configuración de Spring MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CarritoInterceptor carritoInterceptor;
    private final AdminPedidosInterceptor adminPedidosInterceptor;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente los beans de los interceptores.
     */
    public WebConfig(CarritoInterceptor carritoInterceptor, AdminPedidosInterceptor adminPedidosInterceptor) {
        this.carritoInterceptor = carritoInterceptor;
        this.adminPedidosInterceptor = adminPedidosInterceptor;
    }

    /**
     * Override del método inherited de WebMvcConfigurer.
     * Permite registrar interceptores que se ejecutarán en cada request.
     *
     * @param registry El registro de interceptores donde agregamos nuestros interceptores
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor del carrito - aplica a TODAS las rutas
        registry.addInterceptor(carritoInterceptor)
                .addPathPatterns("/**");

        // Interceptor de pedidos pendientes para admin - solo rutas /admin/**
        registry.addInterceptor(adminPedidosInterceptor)
                .addPathPatterns("/admin/**");
    }
}