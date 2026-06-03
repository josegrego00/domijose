package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.user.UserRequest;
import com.josepinodev.appdomirest.dto.user.UserResponse;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private VentaService ventaService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "admin", "pass",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void listarClientes_WithoutQuery_ReturnsAll() throws Exception {
        when(userService.findAllCustomers()).thenReturn(List.of(new UserResponse()));
        when(userService.countCustomers()).thenReturn(1L);
        when(userService.countCustomersActive()).thenReturn(1L);
        when(userService.countCustomersBlocked()).thenReturn(0L);
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/usuarios").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/usuarios-list"))
                .andExpect(model().attributeExists("clientes"))
                .andExpect(model().attribute("totalClientes", 1L));
    }

    @Test
    void listarClientes_WithQuery_Searches() throws Exception {
        when(userService.searchCustomers("test")).thenReturn(List.of(new UserResponse()));
        when(userService.countCustomers()).thenReturn(1L);
        when(userService.countCustomersActive()).thenReturn(0L);
        when(userService.countCustomersBlocked()).thenReturn(0L);
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/usuarios")
                        .with(authentication(auth()))
                        .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searchQuery", "test"));
        verify(userService).searchCustomers("test");
    }

    @Test
    void toggleCliente_Redirects() throws Exception {
        mockMvc.perform(post("/admin/usuarios/{id}/toggle", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios?toggled"));
        verify(userService).toggleActivo(1L);
    }

    @Test
    void listarAdmins_ReturnsView() throws Exception {
        when(userService.findAllAdmins()).thenReturn(List.of(new UserResponse()));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/admins").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admins-list"))
                .andExpect(model().attributeExists("admins"));
    }

    @Test
    void nuevoAdmin_ReturnsForm() throws Exception {
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/admins/nuevo").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin/admin-form"))
                .andExpect(model().attributeExists("admin"));
    }

    @Test
    void guardarAdmin_Success_Redirects() throws Exception {
        when(userService.saveAdmin(any(UserRequest.class))).thenReturn(new UserResponse());

        mockMvc.perform(post("/admin/admins")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("username", "newadmin")
                        .param("password", "password")
                        .param("telefono", "3000000000")
                        .param("email", "admin@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/admins?success"));
    }

    @Test
    void guardarAdmin_Error_ReturnsForm() throws Exception {
        when(userService.saveAdmin(any(UserRequest.class)))
                .thenThrow(new RuntimeException("Error al crear admin"));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(post("/admin/admins")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("username", "newadmin")
                        .param("password", "password")
                        .param("telefono", "3000000000")
                        .param("email", "admin@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin/admin-form"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void editarAdmin_WhenExists_ReturnsForm() throws Exception {
        UserResponse admin = new UserResponse();
        admin.setId(1L);
        admin.setUsername("admin1");
        when(userService.findById(1L)).thenReturn(Optional.of(admin));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/admins/{id}/editar", 1L).with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin/admin-form"))
                .andExpect(model().attributeExists("admin"));
    }

    @Test
    void actualizarAdmin_Success_Redirects() throws Exception {
        when(userService.updateAdmin(any(UserRequest.class), eq(1L))).thenReturn(new UserResponse());

        mockMvc.perform(post("/admin/admins/{id}", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/admins?updated"));
    }

    @Test
    void actualizarAdmin_Error_ReturnsForm() throws Exception {
        when(userService.updateAdmin(any(UserRequest.class), eq(1L)))
                .thenThrow(new RuntimeException("Error"));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(post("/admin/admins/{id}", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin/admin-form"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void eliminarAdmin_Redirects() throws Exception {
        mockMvc.perform(post("/admin/admins/{id}/eliminar", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/admins?deleted"));
        verify(userService).delete(1L);
    }
}
