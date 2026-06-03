package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.ruta.RutaDomicilioDTO;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.RutaDomicilioService;
import com.josepinodev.appdomirest.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RutaDomicilioController.class)
class RutaDomicilioControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private RutaDomicilioService rutaDomicilioService;
    @MockitoBean private VentaService ventaService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "admin", "pass",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void listarRutas_ReturnsView() throws Exception {
        RutaDomicilioDTO r = new RutaDomicilioDTO();
        r.setBarrio("Barrio A");
        RutaDomicilioDTO r2 = new RutaDomicilioDTO();
        r2.setBarrio("Barrio B");
        when(rutaDomicilioService.findAll()).thenReturn(new java.util.ArrayList<>(List.of(r, r2)));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/rutas").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rutas-list"))
                .andExpect(model().attributeExists("rutas"));
    }

    @Test
    void nuevaRuta_ReturnsForm() throws Exception {
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/rutas/nuevo").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ruta/ruta-form"))
                .andExpect(model().attributeExists("ruta"));
    }

    @Test
    void guardarRuta_Redirects() throws Exception {
        when(rutaDomicilioService.save(any(RutaDomicilioDTO.class))).thenReturn(new RutaDomicilioDTO());

        mockMvc.perform(post("/admin/rutas")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("barrio", "Barrio Test")
                        .param("precio", "5000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rutas?success"));
    }

    @Test
    void editarRuta_WhenExists_ReturnsForm() throws Exception {
        RutaDomicilioDTO r = new RutaDomicilioDTO();
        r.setId(1L);
        r.setBarrio("Barrio Test");
        when(rutaDomicilioService.findById(1L)).thenReturn(Optional.of(r));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/rutas/{id}/editar", 1L).with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ruta/ruta-form"))
                .andExpect(model().attributeExists("ruta"));
    }

    @Test
    void actualizarRuta_Redirects() throws Exception {
        when(rutaDomicilioService.update(any(RutaDomicilioDTO.class))).thenReturn(new RutaDomicilioDTO());

        mockMvc.perform(post("/admin/rutas/{id}", 1L)
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("barrio", "Barrio Updated")
                        .param("precio", "6000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rutas?updated"));
    }

    @Test
    void eliminarRuta_Redirects() throws Exception {
        mockMvc.perform(post("/admin/rutas/{id}/eliminar", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rutas?deleted"));

        verify(rutaDomicilioService).delete(1L);
    }
}
