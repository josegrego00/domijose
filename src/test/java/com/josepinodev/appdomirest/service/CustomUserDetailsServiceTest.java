package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.model.UserEntity;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_WithTelefono_ReturnsUserDetails() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded");
        user.setTelefono("3001234567");
        user.setRoles(Set.of());
        user.setActivo(true);

        when(userRepository.findByTelefono("3001234567")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("3001234567");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void loadUserByUsername_WithUsername_ReturnsUserDetails() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded");
        user.setRoles(Set.of());
        user.setActivo(true);

        when(userRepository.findByTelefono("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void loadUserByUsername_WhenNotFound_ThrowsException() {
        when(userRepository.findByTelefono("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown"));
    }
}
