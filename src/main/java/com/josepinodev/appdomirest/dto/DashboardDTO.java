package com.josepinodev.appdomirest.dto;

import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long totalProductos;
    private long totalUsuarios;
    private int productosPromocion;
    private long cantidadPendientes;
    private List<VentaDTO> ventasPendientes;

}
