package com.domicilio.domijose.services;

import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.dto.UserRequest;
import com.domicilio.domijose.mappers.UserMapper;
import com.domicilio.domijose.models.enums.*;
import com.domicilio.domijose.models.User;
import com.domicilio.domijose.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO create(UserRequest request) {
        log.info("Intentando crear usuario con email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Intento de registro con email existente: {}", request.getEmail());
            throw new IllegalArgumentException("El email ya está registrado");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CLIENTE);
        User saved = userRepository.save(user);
        log.info("Usuario creado exitosamente con ID: {}", saved.getId());
        return userMapper.toDTO(saved);
    }

    public UserDTO update(Long id, UserDTO dto) {
        log.info("Actualizando usuario con ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setFullName(dto.getFullName());
        User saved = userRepository.save(user);
        log.info("Usuario actualizado exitosamente: {}", id);
        return userMapper.toDTO(saved);
    }

    public UserDTO findById(Long id) {
        log.debug("Buscando usuario por ID: {}", id);
        return userMapper.toDTO(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado")));
    }

    public UserDTO findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return userMapper.toDTO(userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado")));
    }

    public List<UserDTO> findAll() {
        log.debug("Listando todos los usuarios");
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public void deleteById(Long id) {
        log.info("Eliminando usuario con ID: {}", id);
        userRepository.deleteById(id);
        log.info("Usuario eliminado: {}", id);
    }
}