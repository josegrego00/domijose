package com.josepinodev.appdomirest.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.josepinodev.appdomirest.model.ERole;
import com.josepinodev.appdomirest.model.RoleEntity;
import com.josepinodev.appdomirest.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleDataLoader implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Verificar si ya existen roles (evitar duplicados)
        if (roleRepository.count() == 0) {

            // Crear los 3 roles
            RoleEntity admin = new RoleEntity();
            admin.setName(ERole.ADMIN);
            roleRepository.save(admin);
            RoleEntity user = new RoleEntity();
            user.setName(ERole.USER);
            roleRepository.save(user);
            RoleEntity dev = new RoleEntity();
            dev.setName(ERole.DEV);
            roleRepository.save(dev);

            System.out.println("Roles inicializados: ADMIN, USER, DEV");
        }
    }
}