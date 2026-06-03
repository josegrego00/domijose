package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.carrito.CarritoDTO;
import com.josepinodev.appdomirest.dto.carrito.ItemCarritoDTO;
import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class CarritoService {

    private static final String CARRITO_SESSION_KEY = "carrito";

    private final HttpSession httpSession;

    public CarritoDTO getCarrito() {
        CarritoDTO carrito = (CarritoDTO) httpSession.getAttribute(CARRITO_SESSION_KEY);
        if (carrito == null) {
            carrito = new CarritoDTO();
            httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        return carrito;
    }

    public void agregarProducto(ProductoDTO producto, BigDecimal cantidad, String observacion) {
        CarritoDTO carrito = getCarrito();

        ItemCarritoDTO itemExistente = carrito.findByProductoIdAndObservacion(producto.getId(), observacion);

        if (itemExistente != null) {
            BigDecimal nuevaCantidad = itemExistente.getCantidad().add(cantidad);
            itemExistente.setCantidad(nuevaCantidad);
            log.info("Cantidad actualizada para producto {}: {}", producto.getId(), nuevaCantidad);
        } else {
            BigDecimal precio = producto.getEnPromocion() && producto.getPrecioPromocional() != null
                    ? producto.getPrecioPromocional()
                    : producto.getPrecioVenta();

            ItemCarritoDTO nuevoItem = new ItemCarritoDTO(
                    producto.getId(),
                    producto.getNombre(),
                    precio,
                    producto.getImagenUrl(),
                    cantidad,
                    observacion);
            carrito.addItem(nuevoItem);
            log.info("Producto agregado al carrito: {}", producto.getId());
        }
        carrito.recalcularTotal();

        httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);
    }

    public void actualizarCantidad(Long productoId, BigDecimal cantidad) {
        CarritoDTO carrito = getCarrito();

        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            removerProducto(productoId);
            return;
        }

        carrito.updateCantidad(productoId, cantidad);
        httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);

        log.info("Cantidad actualizada para producto {}: {}", productoId, cantidad);
    }

    public void removerProducto(Long productoId) {
        CarritoDTO carrito = getCarrito();
        carrito.removeItem(productoId);
        httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);

        log.info("Producto removido del carrito: {}", productoId);
    }

    public void limpiarCarrito() {
        CarritoDTO carrito = getCarrito();
        carrito.clear();
        httpSession.setAttribute(CARRITO_SESSION_KEY, carrito);

        log.info("Carrito limpiado");
    }

    public BigDecimal getTotal() {
        return getCarrito().getTotal();
    }

    public BigDecimal getCantidadItems() {
        return getCarrito().getCantidadItems();
    }

    public boolean isEmpty() {
        return getCarrito().getItems().isEmpty();
    }
}