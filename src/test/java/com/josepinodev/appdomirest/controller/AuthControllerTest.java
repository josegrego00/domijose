package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.user.UserRequest;
import com.josepinodev.appdomirest.dto.user.UserResponse;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.AuditService;
import com.josepinodev.appdomirest.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private AuditService auditService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "testuser", "pass",
            List.of(new SimpleGrantedAuthority("USER")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void loginPage_ReturnsOk() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void registerPage_ReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void register_WithValidData_RedirectsToLogin() throws Exception {
        when(userService.findByUsername("nuevouser")).thenReturn(Optional.empty());
        when(userService.existsByTelefono("3001112233")).thenReturn(false);
        when(userService.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(userService.save(any(UserRequest.class))).thenReturn(new UserResponse());

        mockMvc.perform(post("/register")
                        .with(authentication(auth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "nuevouser")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("telefono", "3001112233")
                        .param("email", "nuevo@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(auditService).log(eq("REGISTRO_USUARIO"), anyString());
    }

    @Test
    void register_WithDuplicateUsername_ReturnsFormWithErrors() throws Exception {
        when(userService.findByUsername("existing")).thenReturn(Optional.of(new UserResponse()));

        mockMvc.perform(post("/register")
                        .with(authentication(auth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "existing")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("telefono", "3001112233")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"));
    }

    @Test
    void register_WithDuplicateTelefono_ReturnsFormWithErrors() throws Exception {
        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.existsByTelefono("3001112233")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .with(authentication(auth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("telefono", "3001112233")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"));
    }

    @Test
    void register_WithMismatchedPasswords_ReturnsFormWithErrors() throws Exception {
        when(userService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userService.existsByTelefono("3001112233")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .with(authentication(auth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("confirmPassword", "different")
                        .param("telefono", "3001112233")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"));
    }

    @Test
    void register_WithInvalidData_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/register")
                        .with(authentication(auth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "")
                        .param("password", "123")
                        .param("confirmPassword", "123")
                        .param("telefono", "")
                        .param("email", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void homePage_ReturnsHomeView() throws Exception {
        mockMvc.perform(get("/home").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }
}
