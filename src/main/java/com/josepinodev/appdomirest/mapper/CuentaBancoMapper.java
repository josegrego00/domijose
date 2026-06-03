package com.josepinodev.appdomirest.mapper;

import com.josepinodev.appdomirest.dto.cuenta.CuentaBancoDTO;
import com.josepinodev.appdomirest.model.CuentaBancoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CuentaBancoMapper {

    CuentaBancoDTO toDTO(CuentaBancoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    CuentaBancoEntity toEntity(CuentaBancoDTO dto);
}
