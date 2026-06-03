package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.ProductoService;
import com.josepinodev.appdomirest.service.UserService;
import com.josepinodev.appdomirest.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductoService productoService;
    @MockitoBean private UserService userService;
    @MockitoBean private VentaService ventaService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "admin", "pass",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void dashboard_ReturnsViewWithStats() throws Exception {
        when(productoService.countActive()).thenReturn(15L);
        when(userService.countActive()).thenReturn(10L);
        when(productoService.findEnPromocion()).thenReturn(List.of(new ProductoDTO()));
        when(ventaService.countPendientes()).thenReturn(5L);

        mockMvc.perform(get("/admin").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/home-admin"))
                .andExpect(model().attribute("totalProductos", 15L))
                .andExpect(model().attribute("totalUsuarios", 10L))
                .andExpect(model().attribute("cantidadPendientes", 5L))
                .andExpect(model().attributeExists("productosPromocion"));
    }
}
