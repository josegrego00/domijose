package com.domicilio.domijose.data;

import com.domicilio.domijose.dto.MetodoPagoDTO;
import com.domicilio.domijose.models.MetodoPago;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Data provider for MetodoPago test data
 */
public class DataProviderMetodoPago {

    public static MetodoPago createCuenta() {
        MetodoPago cuenta = new MetodoPago();
        cuenta.setId(1L);
        cuenta.setBanco("Banco de Ejemplo");
        cuenta.setTipoCuenta("CORRIENTE");
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setNombreTitular("Empresa Domijose");
        cuenta.setActivo(true);
        return cuenta;
    }

    public static MetodoPago createQR() {
        MetodoPago qr = new MetodoPago();
        qr.setId(2L);
        qr.setBanco(null);
        qr.setTipoCuenta(null);
        qr.setNumeroCuenta(null);
        qr.setNombreTitular(null);
        qr.setQrUrl("images/qr/qr-example.png");
        qr.setActivo(true);
        return qr;
    }

    public static MetodoPagoDTO createCuentaDTO() {
        MetodoPagoDTO dto = new MetodoPagoDTO();
        dto.setId(1L);
        dto.setBanco("Banco de Ejemplo");
        dto.setTipoCuenta("CORRIENTE");
        dto.setNumeroCuenta("1234567890");
        dto.setNombreTitular("Empresa Domijose");
        dto.setActivo(true);
        return dto;
    }

    public static MetodoPagoDTO createQRDTO() {
        MetodoPagoDTO dto = new MetodoPagoDTO();
        dto.setId(2L);
        dto.setQrUrl("images/qr/qr-example.png");
        dto.setActivo(true);
        return dto;
    }

    public static List<MetodoPago> createMetodoPagoList() {
        List<MetodoPago> lista = new ArrayList<>();
        lista.add(createCuenta());
        lista.add(createQR());
        return lista;
    }

    public static MockMultipartFile createQRFile() {
        return new MockMultipartFile("qr.png", "qr.png", "image/png", "qr-image-content".getBytes());
    }
}
