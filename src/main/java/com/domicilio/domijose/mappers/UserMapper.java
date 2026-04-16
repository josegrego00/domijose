package com.domicilio.domijose.mappers;

import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.dto.UserRequest;
import com.domicilio.domijose.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "orders", ignore = true)
    User toEntity(UserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "orders", ignore = true)
    User toEntity(UserRequest request);

    UserDTO toDTO(User user);
}