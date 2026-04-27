package com.domicilio.domijose.services;

import com.domicilio.domijose.data.DataProviderUser;
import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.mappers.UserMapper;
import com.domicilio.domijose.models.User;
import com.domicilio.domijose.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = DataProviderUser.createUser();
        testUserDTO = DataProviderUser.createUserDTO();
    }

    @Test
    void registerUser_WithValidData_ShouldRegisterUserSuccessfully() {
        // Arrange
        String encodedPassword = "encodedPassword123";
        
        when(userRepository.existsByPhone(testUserDTO.getPhone())).thenReturn(false);
        when(userMapper.toEntity(testUserDTO)).thenReturn(testUser);
        when(passwordEncoder.encode(testUserDTO.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.registerUser(testUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getPhone(), result.getPhone());
        verify(userRepository).existsByPhone(testUserDTO.getPhone());
        verify(passwordEncoder).encode(testUserDTO.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    void registerUser_WithExistingPhone_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByPhone(testUserDTO.getPhone())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(testUserDTO);
        });

        assertEquals("El teléfono ya está registrado", exception.getMessage());
        verify(userRepository).existsByPhone(testUserDTO.getPhone());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUserSuccessfully() {
        // Arrange
        UserDTO updatedDTO = DataProviderUser.createUserDTO();
        updatedDTO.setFullName("Updated Name");
        updatedDTO.setEmail("updated@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(updatedDTO);

        // Act
        UserDTO result = userService.updateUser(testUser.getId(), updatedDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldThrowException() {
        // Arrange
        UserDTO updatedDTO = DataProviderUser.createUserDTO();
        updatedDTO.setFullName("Updated Name");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(99L, updatedDTO);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByPhone_WithExistingPhone_ShouldReturnUserDTO() {
        // Arrange
        when(userRepository.findByPhone(testUser.getPhone())).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.findByPhone(testUser.getPhone());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getPhone(), result.getPhone());
        verify(userRepository).findByPhone(testUser.getPhone());
    }

    @Test
    void findByPhone_WithNonExistingPhone_ShouldThrowException() {
        // Arrange
        when(userRepository.findByPhone("9999999999")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findByPhone("9999999999");
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findByPhone("9999999999");
    }

    @Test
    void findById_WithExistingId_ShouldReturnUserDTO() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.findById(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findById(99L);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findById(99L);
    }
}
