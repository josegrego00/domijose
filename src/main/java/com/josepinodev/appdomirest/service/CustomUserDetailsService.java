package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrTelefono) throws UsernameNotFoundException {
        var user = userRepository.findByTelefono(usernameOrTelefono)
                .orElseGet(() -> userRepository.findByUsername(usernameOrTelefono)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + usernameOrTelefono)));

        return UserDetailsImpl.build(user);
    }
}