package com.domicilio.domijose.mappers;

import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    // DTO → Entidad (para registro/actualización)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserDTO dto);
    
    // Entidad → DTO (para response)
    @Mapping(target = "password", ignore = true)
    @Named("toDto")
    UserDTO toDto(User user);
}