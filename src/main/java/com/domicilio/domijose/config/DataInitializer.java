package com.domicilio.domijose.config;

import com.domicilio.domijose.models.User;
import com.domicilio.domijose.models.enums.Role;
import com.domicilio.domijose.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() {
        // ✅ Mejor: verificar si existe por teléfono (más específico)
        if (!userRepository.existsByPhone("3223470418")) {
            User admin = new User();
            admin.setEmail("josepinodev@gmail.com");
            admin.setPassword(passwordEncoder.encode("Mora.Kristoff"));
            admin.setFullName("DomiJose");
            admin.setPhone("3223470418");
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);

            log.info("========================================");
            log.info("Usuario ADMIN creado exitosamente");
            log.info("Teléfono: 3223470418");
            log.info("Email: josepinodev@gmail.com");
            log.info("Password: Mora.Kristoff");
            log.info("========================================");
        } else {
            log.debug("Ya existe usuario ADMIN, no se crea duplicado");
        }
    }
}