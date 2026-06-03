package com.josepinodev.appdomirest.dto.venta;

import com.josepinodev.appdomirest.model.EMetodoPago;
import com.josepinodev.appdomirest.model.EVentaEstado;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioUsername;
    private String usuarioTelefono;
    private LocalDateTime fecha;
    private EVentaEstado estado;
    private BigDecimal total;
    private String direccion;
    private Long rutaId;
    private String rutaNombre;
    private BigDecimal costoDomicilio;
    private BigDecimal subtotal;
    private Boolean esDomicilio;
    private EMetodoPago metodoPago;
    private BigDecimal efectivoRecibido;
    private BigDecimal efectivoCambio;
    private BigDecimal montoTransferencia;
    private Long cuentaBancoId;
    private String cuentaBancoNombre;
    private String cuentaBancoTipo;
    private String cuentaBancoTitular;
    private String cuentaBancoQrUrl;
    private List<DetalleVentaDTO> detalles = new ArrayList<>();
    private Boolean activo = true;

    public BigDecimal getTotalGeneral() {
        BigDecimal productos = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal domicilio = costoDomicilio != null ? costoDomicilio : BigDecimal.ZERO;
        return productos.add(domicilio);
    }
}