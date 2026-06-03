package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.DashboardUpdateEvent;
import com.josepinodev.appdomirest.dto.role.RoleDTO;
import com.josepinodev.appdomirest.dto.user.UserRequest;
import com.josepinodev.appdomirest.dto.user.UserResponse;
import com.josepinodev.appdomirest.mapper.RoleMapper;
import com.josepinodev.appdomirest.mapper.UserMapper;
import com.josepinodev.appdomirest.model.ERole;
import com.josepinodev.appdomirest.model.RoleEntity;
import com.josepinodev.appdomirest.model.UserEntity;
import com.josepinodev.appdomirest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDTO);
    }

    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDTO);
    }

    public boolean existsByTelefono(String telefono) {
        return userRepository.existsByTelefono(telefono);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserResponse save(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya esta en uso");
        }
        if (userRepository.existsByTelefono(request.getTelefono())) {
            throw new RuntimeException("El telefono ya esta en uso");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya esta en uso");
        }

        UserEntity user = userMapper.toEntityWithPassword(request, passwordEncoder.encode(request.getPassword()));

        if (request.getDireccion() != null && !request.getDireccion().isEmpty()) {
            List<String> direcciones = new ArrayList<>();
            direcciones.add(request.getDireccion());
            user.setDirecciones(direcciones);
        }

        Set<RoleEntity> roles = new HashSet<>();
        RoleDTO roleDTO = roleService.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
        roles.add(roleMapper.toEntity(roleDTO));
        user.setRoles(roles);

        UserResponse response = userMapper.toDTO(userRepository.save(user));
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        return response;
    }

    public UserResponse update(UserRequest request, Long id) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!existingUser.getUsername().equals(request.getUsername()) 
                && userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya esta en uso");
        }
        if (!Objects.equals(existingUser.getTelefono(), request.getTelefono()) 
                && userRepository.existsByTelefono(request.getTelefono())) {
            throw new RuntimeException("El telefono ya esta en uso");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!Objects.equals(request.getEmail(), existingUser.getEmail()) 
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El email ya esta en uso");
            }
        }

        existingUser.setUsername(request.getUsername());
        existingUser.setTelefono(request.getTelefono());
        existingUser.setEmail(request.getEmail());
        if (request.getDireccion() != null && !request.getDireccion().isEmpty()) {
            List<String> direcciones = new ArrayList<>();
            direcciones.add(request.getDireccion());
            existingUser.setDirecciones(direcciones);
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toDTO(userRepository.save(existingUser));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActivo(false);
            userRepository.save(user);
            eventPublisher.publishEvent(new DashboardUpdateEvent(this));
            auditService.log("ELIMINAR_ADMIN", "Admin #" + id + ": " + user.getUsername() + " eliminado (soft-delete)");
        });
    }

    public Long countActive() {
        return userRepository.countByActivo(true);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void toggleActivo(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActivo(!user.getActivo());
            userRepository.save(user);
            eventPublisher.publishEvent(new DashboardUpdateEvent(this));
            auditService.log("TOGGLE_USUARIO", "Usuario #" + id + ": " + user.getUsername() + " activo=" + user.getActivo());
        });
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> findAllCustomers() {
        return userRepository.findByRoles_Name(ERole.USER).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> searchCustomers(String query) {
        return userRepository.searchByUsernameOrTelefono(ERole.USER, query).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Long countCustomers() {
        return (long) userRepository.findByRoles_Name(ERole.USER).size();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Long countCustomersActive() {
        return (long) userRepository.findByRoles_NameAndActivo(ERole.USER, true).size();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public Long countCustomersBlocked() {
        return (long) userRepository.findByRoles_NameAndActivo(ERole.USER, false).size();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> findAllAdmins() {
        return userRepository.findByRoles_Name(ERole.ADMIN).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse saveAdmin(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya esta en uso");
        }
        if (userRepository.existsByTelefono(request.getTelefono())) {
            throw new RuntimeException("El telefono ya esta en uso");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya esta en uso");
        }

        UserEntity user = userMapper.toEntityWithPassword(request, passwordEncoder.encode(request.getPassword()));

        if (request.getDireccion() != null && !request.getDireccion().isEmpty()) {
            List<String> direcciones = new ArrayList<>();
            direcciones.add(request.getDireccion());
            user.setDirecciones(direcciones);
        }

        Set<RoleEntity> roles = new HashSet<>();
        RoleDTO roleDTO = roleService.findByName(ERole.ADMIN)
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
        roles.add(roleMapper.toEntity(roleDTO));
        user.setRoles(roles);

        UserResponse response = userMapper.toDTO(userRepository.save(user));
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("CREAR_ADMIN", "Admin " + response.getUsername() + " creado");
        return response;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse updateAdmin(UserRequest request, Long id) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!existingUser.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya esta en uso");
        }
        if (!Objects.equals(existingUser.getTelefono(), request.getTelefono())
                && userRepository.existsByTelefono(request.getTelefono())) {
            throw new RuntimeException("El telefono ya esta en uso");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!Objects.equals(request.getEmail(), existingUser.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El email ya esta en uso");
            }
        }

        existingUser.setUsername(request.getUsername());
        existingUser.setTelefono(request.getTelefono());
        existingUser.setEmail(request.getEmail());
        if (request.getDireccion() != null && !request.getDireccion().isEmpty()) {
            List<String> direcciones = new ArrayList<>();
            direcciones.add(request.getDireccion());
            existingUser.setDirecciones(direcciones);
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        UserResponse response = userMapper.toDTO(userRepository.save(existingUser));
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("EDITAR_ADMIN", "Admin #" + id + ": " + existingUser.getUsername() + " actualizado");
        return response;
    }
}