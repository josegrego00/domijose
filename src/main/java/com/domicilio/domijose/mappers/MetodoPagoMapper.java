package com.domicilio.domijose.mappers;

import com.domicilio.domijose.dto.MetodoPagoDTO;
import com.domicilio.domijose.models.MetodoPago;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MetodoPagoMapper {
    MetodoPagoDTO toDto(MetodoPago entity);
    MetodoPago toEntity(MetodoPagoDTO dto);
}