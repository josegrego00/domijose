package com.josepinodev.appdomirest.mapper;

import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.model.ProductoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "usuarioCreadorId", source = "usuarioCreador.id")
    ProductoDTO toDTO(ProductoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "usuarioCreador", ignore = true)
    ProductoEntity toEntity(ProductoDTO dto);
}