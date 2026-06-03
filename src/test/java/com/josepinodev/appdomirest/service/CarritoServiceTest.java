package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.carrito.CarritoDTO;
import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock private HttpSession httpSession;
    @InjectMocks private CarritoService carritoService;

    private ProductoDTO producto;
    private CarritoDTO carrito;

    @BeforeEach
    void setUp() {
        producto = new ProductoDTO();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecioVenta(new BigDecimal("10000"));
        producto.setImagenUrl("/img.jpg");

        carrito = new CarritoDTO();
    }

    @Test
    void getCarrito_WhenExists_ReturnsFromSession() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);

        var result = carritoService.getCarrito();

        assertNotNull(result);
        verify(httpSession, never()).setAttribute(anyString(), any());
    }

    @Test
    void getCarrito_WhenNotExists_CreatesNew() {
        when(httpSession.getAttribute("carrito")).thenReturn(null);

        var result = carritoService.getCarrito();

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        verify(httpSession).setAttribute(eq("carrito"), any(CarritoDTO.class));
    }

    @Test
    void agregarProducto_NewItem_AddsToCart() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);

        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        assertEquals(1, carrito.getItems().size());
        assertEquals("Producto Test", carrito.getItems().get(0).getNombre());
        verify(httpSession).setAttribute(eq("carrito"), any(CarritoDTO.class));
    }

    @Test
    void agregarProducto_DuplicateItem_IncrementsQuantity() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        assertEquals(1, carrito.getItems().size());
        assertEquals(new BigDecimal("2"), carrito.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_WithPromotion_UsesPromoPrice() {
        producto.setEnPromocion(true);
        producto.setPrecioVenta(new BigDecimal("10000"));
        producto.setPrecioPromocional(new BigDecimal("8000"));
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);

        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        assertEquals(new BigDecimal("8000"), carrito.getItems().get(0).getPrecio());
        assertEquals(new BigDecimal("8000"), carrito.getItems().get(0).getSubtotal());
    }

    @Test
    void actualizarCantidad_Positive_Updates() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        carritoService.actualizarCantidad(1L, new BigDecimal("3"));

        assertEquals(new BigDecimal("3"), carrito.getItems().get(0).getCantidad());
    }

    @Test
    void actualizarCantidad_Zero_RemovesItem() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        carritoService.actualizarCantidad(1L, BigDecimal.ZERO);

        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void removerProducto_RemovesItem() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        carritoService.removerProducto(1L);

        assertTrue(carrito.getItems().isEmpty());
        verify(httpSession, times(2)).setAttribute(eq("carrito"), any(CarritoDTO.class));
    }

    @Test
    void limpiarCarrito_ClearsCart() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        carritoService.limpiarCarrito();

        assertTrue(carrito.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, carrito.getTotal());
    }

    @Test
    void getTotal_ReturnsSum() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        assertEquals(new BigDecimal("10000"), carritoService.getTotal());
    }

    @Test
    void getCantidadItems_ReturnsCount() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, new BigDecimal("2"), null);

        assertEquals(new BigDecimal("2"), carritoService.getCantidadItems());
    }

    @Test
    void isEmpty_WhenEmpty_ReturnsTrue() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);

        assertTrue(carritoService.isEmpty());
    }

    @Test
    void isEmpty_WhenNotEmpty_ReturnsFalse() {
        when(httpSession.getAttribute("carrito")).thenReturn(carrito);
        carritoService.agregarProducto(producto, BigDecimal.ONE, null);

        assertFalse(carritoService.isEmpty());
    }
}
