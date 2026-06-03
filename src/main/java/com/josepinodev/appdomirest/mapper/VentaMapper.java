package com.josepinodev.appdomirest.mapper;

import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import com.josepinodev.appdomirest.model.VentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioUsername", source = "usuario.username")
    @Mapping(target = "usuarioTelefono", source = "usuario.telefono")
    @Mapping(target = "rutaId", source = "rutaDomicilio.id")
    @Mapping(target = "rutaNombre", source = "rutaDomicilio.barrio")
    @Mapping(target = "cuentaBancoId", source = "cuentaBanco.id")
    @Mapping(target = "cuentaBancoNombre", source = "cuentaBanco.banco")
    @Mapping(target = "cuentaBancoTipo", source = "cuentaBanco.tipoCuenta")
    @Mapping(target = "cuentaBancoTitular", source = "cuentaBanco.titular")
    @Mapping(target = "cuentaBancoQrUrl", source = "cuentaBanco.imagenQrUrl")
    @Mapping(target = "detalles", ignore = true)
    VentaDTO toDTO(VentaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    @Mapping(target = "rutaDomicilio", ignore = true)
    @Mapping(target = "cuentaBanco", ignore = true)
    VentaEntity toEntity(VentaDTO dto);

    List<VentaDTO> toDTOList(List<VentaEntity> entities);
}
