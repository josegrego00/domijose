package com.josepinodev.appdomirest.mapper;

import com.josepinodev.appdomirest.dto.ruta.RutaDomicilioDTO;
import com.josepinodev.appdomirest.model.RutaDomicilioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RutaDomicilioMapper {

    RutaDomicilioDTO toDTO(RutaDomicilioEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    RutaDomicilioEntity toEntity(RutaDomicilioDTO dto);
}