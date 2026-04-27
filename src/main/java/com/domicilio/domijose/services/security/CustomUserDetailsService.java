package com.domicilio.domijose.services.security;

import com.domicilio.domijose.models.User;
import com.domicilio.domijose.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        log.debug("Cargando usuario por telefono: {}", phone);
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", phone);
                    return new UsernameNotFoundException("Usuario no encontrado: " + phone);
                });
        log.debug("Usuario encontrado: {}", phone);
        return new CustomUserDetails(user);
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + phone));
    }
}