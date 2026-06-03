package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.role.RoleDTO;
import com.josepinodev.appdomirest.mapper.RoleMapper;
import com.josepinodev.appdomirest.model.ERole;
import com.josepinodev.appdomirest.model.RoleEntity;
import com.josepinodev.appdomirest.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private RoleMapper roleMapper;
    @InjectMocks private RoleService roleService;

    private RoleEntity entity;
    private RoleDTO dto;

    @BeforeEach
    void setUp() {
        entity = new RoleEntity(1L, ERole.USER);
        dto = new RoleDTO(1L, ERole.USER);
    }

    @Test
    void findAll_ReturnsAllRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(entity));
        when(roleMapper.toDTO(entity)).thenReturn(dto);

        var result = roleService.findAll();

        assertEquals(1, result.size());
        assertEquals(ERole.USER, result.get(0).getName());
    }

    @Test
    void findById_WhenExists_ReturnsDTO() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(roleMapper.toDTO(entity)).thenReturn(dto);

        var result = roleService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(ERole.USER, result.get().getName());
    }

    @Test
    void findById_WhenNotExists_ReturnsEmpty() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        var result = roleService.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByName_WhenExists_ReturnsDTO() {
        when(roleRepository.findByName(ERole.USER)).thenReturn(Optional.of(entity));
        when(roleMapper.toDTO(entity)).thenReturn(dto);

        var result = roleService.findByName(ERole.USER);

        assertTrue(result.isPresent());
        assertEquals(ERole.USER, result.get().getName());
    }

    @Test
    void findByName_WhenNotExists_ReturnsEmpty() {
        when(roleRepository.findByName(ERole.ADMIN)).thenReturn(Optional.empty());

        var result = roleService.findByName(ERole.ADMIN);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ReturnsSavedDTO() {
        when(roleMapper.toEntity(dto)).thenReturn(entity);
        when(roleRepository.save(entity)).thenReturn(entity);
        when(roleMapper.toDTO(entity)).thenReturn(dto);

        var result = roleService.save(dto);

        assertNotNull(result);
        assertEquals(ERole.USER, result.getName());
    }

    @Test
    void update_ReturnsUpdatedDTO() {
        when(roleMapper.toEntity(dto)).thenReturn(entity);
        when(roleRepository.save(entity)).thenReturn(entity);
        when(roleMapper.toDTO(entity)).thenReturn(dto);

        var result = roleService.update(dto);

        assertNotNull(result);
    }

    @Test
    void delete_CallsRepository() {
        roleService.delete(1L);

        verify(roleRepository).deleteById(1L);
    }
}
