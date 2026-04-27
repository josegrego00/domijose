package com.domicilio.domijose.data;

import com.domicilio.domijose.dto.CarritoItemDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data provider for Carrito test data
 */
public class DataProviderCarrito {

    public static Map<Long, Integer> createCarritoMap() {
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2); // Producto 1, cantidad 2
        carrito.put(2L, 1); // Producto 2, cantidad 1
        return carrito;
    }

    public static CarritoItemDTO createCarritoItemDTO() {
        return new CarritoItemDTO(
            1L,
            "Producto Test",
            "imagen-test.jpg",
            new BigDecimal("25.00"),
            2
        );
    }

    public static List<CarritoItemDTO> createCarritoItemList() {
        List<CarritoItemDTO> items = new ArrayList<>();
        items.add(new CarritoItemDTO(
            1L,
            "Producto Test",
            "imagen-test.jpg",
            new BigDecimal("25.00"),
            2
        ));
        items.add(new CarritoItemDTO(
            2L,
            "Otro Producto",
            "otra-imagen.jpg",
            new BigDecimal("30.00"),
            1
        ));
        return items;
    }
}
