package com.domicilio.domijose.services;

import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.mappers.UserMapper;
import com.domicilio.domijose.models.enums.Role;
import com.domicilio.domijose.models.User;
import com.domicilio.domijose.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserDTO registerUser(UserDTO userDTO) {
        log.info("Registrando usuario con teléfono: {}", userDTO.getPhone());
        
        // Validar teléfono único
        if (userRepository.existsByPhone(userDTO.getPhone())) {
            throw new IllegalArgumentException("El teléfono ya está registrado");
        }
        
        // Convertir DTO a entidad
        User user = userMapper.toEntity(userDTO);
        user.setEmail(null); // Asegurar que email es null, ya que es opcional
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(Role.CLIENTE);
        
        User saved = userRepository.save(user);
        log.info("Usuario registrado con ID: {}", saved.getId());
        
        return userMapper.toDto(saved);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Actualizando usuario ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (userDTO.getFullName() != null) user.setFullName(userDTO.getFullName());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        // No actualizar phone ni password aquí
        
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public UserDTO findByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return userMapper.toDto(user);
    }
    
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return userMapper.toDto(user);
    }
}