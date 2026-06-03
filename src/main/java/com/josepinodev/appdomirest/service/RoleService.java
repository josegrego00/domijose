package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.role.RoleDTO;
import com.josepinodev.appdomirest.mapper.RoleMapper;
import com.josepinodev.appdomirest.model.ERole;
import com.josepinodev.appdomirest.model.RoleEntity;
import com.josepinodev.appdomirest.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<RoleDTO> findAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDTO)
                .toList();
    }

    public Optional<RoleDTO> findById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toDTO);
    }

    public Optional<RoleDTO> findByName(ERole name) {
        return roleRepository.findByName(name)
                .map(roleMapper::toDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public RoleDTO save(RoleDTO dto) {
        RoleEntity entity = roleMapper.toEntity(dto);
        return roleMapper.toDTO(roleRepository.save(entity));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public RoleDTO update(RoleDTO dto) {
        RoleEntity entity = roleMapper.toEntity(dto);
        return roleMapper.toDTO(roleRepository.save(entity));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}