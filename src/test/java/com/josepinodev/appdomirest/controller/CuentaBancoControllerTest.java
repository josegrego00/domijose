package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.cuenta.CuentaBancoDTO;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.CuentaBancoService;
import com.josepinodev.appdomirest.service.ImageService;
import com.josepinodev.appdomirest.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

@WebMvcTest(CuentaBancoController.class)
class CuentaBancoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CuentaBancoService cuentaBancoService;
    @MockitoBean private VentaService ventaService;
    @MockitoBean private ImageService imageService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "admin", "pass",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void listarCuentas_ReturnsView() throws Exception {
        when(cuentaBancoService.findAll()).thenReturn(List.of(new CuentaBancoDTO()));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/cuentas-banco").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/cuentas-banco-list"))
                .andExpect(model().attributeExists("cuentas"));
    }

    @Test
    void nuevaCuenta_ReturnsForm() throws Exception {
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/cuentas-banco/nuevo").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/cuenta-banco/cuenta-form"))
                .andExpect(model().attributeExists("cuenta"));
    }

    @Test
    void guardarCuenta_WithoutQR_Redirects() throws Exception {
        when(cuentaBancoService.save(any(CuentaBancoDTO.class))).thenReturn(new CuentaBancoDTO());

        MockMultipartFile emptyQr = new MockMultipartFile("qrFile", new byte[0]);

        mockMvc.perform(multipart("/admin/cuentas-banco")
                        .file(emptyQr)
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("banco", "Banco Test")
                        .param("tipoCuenta", "Ahorros")
                        .param("numeroCuenta", "12345")
                        .param("titular", "Test User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cuentas-banco?success"));
    }

    @Test
    void editarCuenta_WhenExists_ReturnsForm() throws Exception {
        CuentaBancoDTO c = new CuentaBancoDTO();
        c.setId(1L);
        c.setBanco("Banco Test");
        when(cuentaBancoService.findById(1L)).thenReturn(Optional.of(c));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/cuentas-banco/{id}/editar", 1L).with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/cuenta-banco/cuenta-form"))
                .andExpect(model().attributeExists("cuenta"));
    }

    @Test
    void actualizarCuenta_Redirects() throws Exception {
        CuentaBancoDTO existing = new CuentaBancoDTO();
        existing.setId(1L);
        existing.setBanco("Old Banco");
        when(cuentaBancoService.findById(1L)).thenReturn(Optional.of(existing));
        when(cuentaBancoService.update(any(CuentaBancoDTO.class))).thenReturn(existing);

        MockMultipartFile emptyQr = new MockMultipartFile("qrFile", new byte[0]);

        mockMvc.perform(multipart("/admin/cuentas-banco/{id}", 1L)
                        .file(emptyQr)
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("banco", "Updated Banco")
                        .param("tipoCuenta", "Corriente")
                        .param("numeroCuenta", "67890")
                        .param("titular", "Updated User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cuentas-banco?updated"));
    }

    @Test
    void eliminarCuenta_Redirects() throws Exception {
        mockMvc.perform(post("/admin/cuentas-banco/{id}/eliminar", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cuentas-banco?deleted"));

        verify(cuentaBancoService).delete(1L);
    }
}
