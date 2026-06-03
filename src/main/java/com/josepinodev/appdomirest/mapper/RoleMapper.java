package com.josepinodev.appdomirest.mapper;

import com.josepinodev.appdomirest.dto.role.RoleDTO;
import com.josepinodev.appdomirest.model.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDTO toDTO(RoleEntity entity);

    RoleEntity toEntity(RoleDTO dto);
}