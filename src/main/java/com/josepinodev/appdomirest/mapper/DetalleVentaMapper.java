package com.josepinodev.appdomirest.mapper;

import com.josepinodev.appdomirest.dto.venta.DetalleVentaDTO;
import com.josepinodev.appdomirest.model.DetalleVentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DetalleVentaMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "productoImagenUrl", source = "producto.imagenUrl")
    @Mapping(target = "observacion", source = "observacion")
    DetalleVentaDTO toDTO(DetalleVentaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "producto", ignore = true)
    DetalleVentaEntity toEntity(DetalleVentaDTO dto);

    List<DetalleVentaDTO> toDTOList(List<DetalleVentaEntity> entities);
}