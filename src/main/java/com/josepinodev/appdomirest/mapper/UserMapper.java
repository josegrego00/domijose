package com.josepinodev.appdomirest.mapper;

import com.josepinodev.appdomirest.dto.user.UserRequest;
import com.josepinodev.appdomirest.dto.user.UserResponse;
import com.josepinodev.appdomirest.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    UserEntity toEntity(UserRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    @Mapping(target = "password", source = "encryptedPassword")
    UserEntity toEntityWithPassword(UserRequest dto, String encryptedPassword);

    UserResponse toDTO(UserEntity entity);
}