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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleMapper roleMapper;
    @Mock private RoleService roleService;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private UserService userService;

    private UserEntity entity;
    private UserResponse response;
    private UserRequest request;
    private RoleEntity roleUser;
    private RoleEntity roleAdmin;
    private RoleDTO roleUserDTO;
    private RoleDTO roleAdminDTO;

    @BeforeEach
    void setUp() {
        roleUser = new RoleEntity(1L, ERole.USER);
        roleAdmin = new RoleEntity(2L, ERole.ADMIN);
        roleUserDTO = new RoleDTO(1L, ERole.USER);
        roleAdminDTO = new RoleDTO(2L, ERole.ADMIN);

        entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("testuser");
        entity.setPassword("encoded");
        entity.setTelefono("3001234567");
        entity.setEmail("test@example.com");
        entity.setActivo(true);
        entity.setRoles(Set.of(roleUser));

        response = new UserResponse();
        response.setId(1L);
        response.setUsername("testuser");
        response.setTelefono("3001234567");
        response.setEmail("test@example.com");
        response.setActivo(true);

        request = new UserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setTelefono("3001234567");
        request.setEmail("test@example.com");
    }

    @Test
    void findAll_ReturnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void findById_WhenExists_ReturnsDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findById_WhenNotExists_ReturnsEmpty() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        var result = userService.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_WhenExists_ReturnsDTO() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void existsByTelefono_WhenExists_ReturnsTrue() {
        when(userRepository.existsByTelefono("3001234567")).thenReturn(true);

        assertTrue(userService.existsByTelefono("3001234567"));
    }

    @Test
    void existsByEmail_WhenExists_ReturnsTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    void save_WithUniqueData_CreatesUser() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByTelefono("3001234567")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userMapper.toEntityWithPassword(request, "encoded")).thenReturn(entity);
        when(roleService.findByName(ERole.USER)).thenReturn(Optional.of(roleUserDTO));
        when(roleMapper.toEntity(roleUserDTO)).thenReturn(roleUser);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.save(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(eventPublisher).publishEvent(any(DashboardUpdateEvent.class));
    }

    @Test
    void save_WithDuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.save(request));
    }

    @Test
    void save_WithDuplicateTelefono_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByTelefono("3001234567")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.save(request));
    }

    @Test
    void save_WithDuplicateEmail_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByTelefono("3001234567")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.save(request));
    }

    @Test
    void update_WhenExists_UpdatesUser() {
        UserRequest updateReq = new UserRequest();
        updateReq.setUsername("updateduser");
        updateReq.setTelefono("3007654321");
        updateReq.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByTelefono("3007654321")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(entity);
        lenient().when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.update(updateReq, 1L);

        assertNotNull(result);
        verify(userRepository).save(entity);
    }

    @Test
    void update_WhenNotExists_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.update(request, 99L));
    }

    @Test
    void update_WithDuplicateUsername_ThrowsException() {
        UserEntity existing = new UserEntity();
        existing.setId(1L);
        existing.setUsername("existing");
        existing.setTelefono("3000000000");

        UserRequest updateReq = new UserRequest();
        updateReq.setUsername("taken");
        updateReq.setTelefono("3000000000");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.update(updateReq, 1L));
    }

    @Test
    void delete_WhenExists_MarksInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        userService.delete(1L);

        assertFalse(entity.getActivo());
        verify(userRepository).save(entity);
        verify(auditService).log(eq("ELIMINAR_ADMIN"), anyString());
    }

    @Test
    void delete_WhenNotExists_DoesNothing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        userService.delete(99L);

        verify(userRepository, never()).save(any());
    }

    @Test
    void countActive_ReturnsCount() {
        when(userRepository.countByActivo(true)).thenReturn(10L);

        assertEquals(10L, userService.countActive());
    }

    @Test
    void toggleActivo_WhenExists_Toggles() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        userService.toggleActivo(1L);

        assertFalse(entity.getActivo());
        verify(userRepository).save(entity);
        verify(auditService).log(eq("TOGGLE_USUARIO"), anyString());
    }

    @Test
    void findAllCustomers_ReturnsOnlyUserRole() {
        when(userRepository.findByRoles_Name(ERole.USER)).thenReturn(List.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.findAllCustomers();

        assertEquals(1, result.size());
    }

    @Test
    void searchCustomers_ReturnsMatching() {
        when(userRepository.searchByUsernameOrTelefono(ERole.USER, "test")).thenReturn(List.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.searchCustomers("test");

        assertEquals(1, result.size());
    }

    @Test
    void countCustomers_ReturnsCount() {
        when(userRepository.findByRoles_Name(ERole.USER)).thenReturn(List.of(entity, new UserEntity()));

        assertEquals(2L, userService.countCustomers());
    }

    @Test
    void countCustomersActive_ReturnsActiveCount() {
        when(userRepository.findByRoles_NameAndActivo(ERole.USER, true)).thenReturn(List.of(entity));

        assertEquals(1L, userService.countCustomersActive());
    }

    @Test
    void countCustomersBlocked_ReturnsBlockedCount() {
        when(userRepository.findByRoles_NameAndActivo(ERole.USER, false)).thenReturn(List.of());

        assertEquals(0L, userService.countCustomersBlocked());
    }

    @Test
    void findAllAdmins_ReturnsOnlyAdminRole() {
        when(userRepository.findByRoles_Name(ERole.ADMIN)).thenReturn(List.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.findAllAdmins();

        assertEquals(1, result.size());
    }

    @Test
    void saveAdmin_WithUniqueData_CreatesAdmin() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByTelefono("3001234567")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userMapper.toEntityWithPassword(request, "encoded")).thenReturn(entity);
        when(roleService.findByName(ERole.ADMIN)).thenReturn(Optional.of(roleAdminDTO));
        when(roleMapper.toEntity(roleAdminDTO)).thenReturn(roleAdmin);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.saveAdmin(request);

        assertNotNull(result);
        verify(auditService).log(eq("CREAR_ADMIN"), anyString());
    }

    @Test
    void saveAdmin_WithDuplicateTelefono_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByTelefono("3001234567")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.saveAdmin(request));
    }

    @Test
    void updateAdmin_WhenExists_UpdatesAdmin() {
        UserRequest updateReq = new UserRequest();
        updateReq.setUsername("updatedadmin");
        updateReq.setTelefono("3007654321");
        updateReq.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.existsByUsername("updatedadmin")).thenReturn(false);
        when(userRepository.existsByTelefono("3007654321")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(entity);
        lenient().when(userMapper.toDTO(entity)).thenReturn(response);

        var result = userService.updateAdmin(updateReq, 1L);

        assertNotNull(result);
        verify(auditService).log(eq("EDITAR_ADMIN"), anyString());
    }

    @Test
    void updateAdmin_WhenNotExists_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateAdmin(request, 99L));
    }
}
