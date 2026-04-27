package com.domicilio.domijose.services;

import com.domicilio.domijose.data.DataProviderCarrito;
import com.domicilio.domijose.data.DataProviderProduct;
import com.domicilio.domijose.dto.CarritoItemDTO;
import com.domicilio.domijose.dto.OrderItemDTO;
import com.domicilio.domijose.dto.ProductDTO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private CarritoService carritoService;

    private Map<Long, Integer> carritoMap;
    private ProductDTO testProductDTO;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        carritoMap = DataProviderCarrito.createCarritoMap();
        when(session.getAttribute("carrito")).thenReturn(carritoMap);
        
        testProductDTO = createTestProductDTO();
        
        file = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", "image-content".getBytes());
    }
    
    private ProductDTO createTestProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setName("Test Product");
        dto.setPrice(new BigDecimal("25.00"));
        dto.setStock(10);
        dto.setImageUrl("test-image.jpg");
        dto.setAvailable(true);
        dto.setCategory("PLATO_FUERTE");
        return dto;
    }
    
    private ProductDTO createTestProductDTO2() {
        ProductDTO dto = new ProductDTO();
        dto.setId(2L);
        dto.setName("Otro Producto");
        dto.setPrice(new BigDecimal("30.00"));
        dto.setStock(10);
        dto.setImageUrl("otra-imagen.jpg");
        dto.setAvailable(true);
        dto.setCategory("POSTRE");
        return dto;
    }

    @Test
    void agregarProducto_WithValidData_ShouldAddToCarrito() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);

        // Act
        boolean result = carritoService.agregarProducto(1L, 1, session);

        // Assert
        assertTrue(result);
        assertEquals(3, carritoMap.get(1L)); // 2 original + 1 nuevo
    }

    @Test
    void agregarProducto_WithInsufficientStock_ShouldReturnFalse() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);

        // Act & Assert
        boolean result = carritoService.agregarProducto(1L, 10, session);
        
        assertFalse(result);
        assertNull(carritoMap.get(1L)); // No debería agregar
    }

    @Test
    void agregarProducto_WhenProductNotExists_ShouldReturnFalse() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenThrow(new IllegalArgumentException("Producto no encontrado"));

        // Act & Assert
        boolean result = carritoService.agregarProducto(1L, 1, session);
        
        assertFalse(result);
        verify(productService).getProductByIdForPublic(1L);
    }

    @Test
    void actualizarCantidad_WithValidData_ShouldUpdateQuantity() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);

        // Act
        boolean result = carritoService.actualizarCantidad(1L, 5, session);

        // Assert
        assertTrue(result);
        assertEquals(5, carritoMap.get(1L));
    }

    @Test
    void actualizarCantidad_WithQuantityZero_ShouldRemoveFromCarrito() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);

        // Act
        boolean result = carritoService.actualizarCantidad(1L, 0, session);

        // Assert
        assertTrue(result);
        assertFalse(carritoMap.containsKey(1L)); // Debería ser eliminado
    }

    @Test
    void actualizarCantidad_WithInsufficientStock_ShouldReturnFalse() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);

        // Act & Assert
        boolean result = carritoService.actualizarCantidad(1L, 20, session);
        
        assertFalse(result);
        assertEquals(2, carritoMap.get(1L)); // No debería cambiar
    }

    @Test
    void actualizarCantidad_WhenProductNotInCarrito_ShouldReturnFalse() {
        // Arrange
        when(productService.getProductByIdForPublic(99L)).thenReturn(testProductDTO);

        // Act & Assert
        boolean result = carritoService.actualizarCantidad(99L, 1, session);
        
        assertFalse(result);
    }

    @Test
    void eliminarProducto_ShouldRemoveFromCarrito() {
        // Arrange
        doNothing().when(session).setAttribute(eq("carrito"), any(Map.class));

        // Act
        carritoService.eliminarProducto(1L, session);

        // Assert
        assertFalse(carritoMap.containsKey(1L));
    }

    @Test
    void obtenerCarritoParaVista_ShouldReturnCarritoItemList() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);
        when(productService.getProductByIdForPublic(2L)).thenReturn(DataProviderProduct.createProductDTO2());

        // Act
        List<CarritoItemDTO> result = carritoService.obtenerCarritoParaVista(session);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getProductId());
        assertEquals(50.00, result.get(0).getSubtotal().doubleValue()); // 25 * 2
    }

    @Test
    void obtenerCarritoParaViu_WithEmptyCarrito_ShouldReturnEmptyList() {
        // Arrange
        carritoMap.clear();
        when(session.getAttribute("carrito")).thenReturn(carritoMap);

        // Act
        List<CarritoItemDTO> result = carritoService.obtenerCarritoParaVista(session);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void obtenerTotal_ShouldCalculateTotal() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);
        when(productService.getProductByIdForPublic(2L)).thenReturn(DataProviderProduct.createProductDTO2());

        // Act
        BigDecimal total = carritoService.obtenerTotal(session);

        // Assert
        assertNotNull(total);
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void convertirParaPedido_WithValidCarrito_ShouldReturnOrderItemDTOs() {
        // Arrange
        when(productService.getProductByIdForPublic(1L)).thenReturn(testProductDTO);
        when(productService.getProductByIdForPublic(2L)).thenReturn(DataProviderProduct.createProductDTO2());

        // Act
        List<OrderItemDTO> result = carritoService.convertirParaPedido(session);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getProductId());
        assertEquals(2, result.get(0).getQuantity());
    }

    @Test
    void convertirParaPedido_WithInsufficientStock_ShouldReturnNull() {
        // Arrange
        ProductDTO lowStockProduct = DataProviderProduct.createProductDTO();
        lowStockProduct.setStock(1); // Stock insuficiente

        when(productService.getProductByIdForPublic(1L)).thenReturn(lowStockProduct);
        when(productService.getProductByIdForPublic(2L)).thenReturn(DataProviderProduct.createProductDTO2());

        // Act & Assert
        List<OrderItemDTO> result = carritoService.convertirParaPedido(session);

        assertNull(result);
    }

    @Test
    void vaciarCarrito_ShouldRemoveCarritoFromSession() {
        // Arrange
        doNothing().when(session).removeAttribute("carrito");

        // Act
        carritoService.vaciarCarrito(session);

        // Assert
        assertTrue(carritoMap.isEmpty());
    }

    @Test
    void estaVacio_WithEmptyCarrito_ShouldReturnTrue() {
        // Arrange
        carritoMap.clear();
        when(session.getAttribute("carrito")).thenReturn(carritoMap);

        // Act & Assert
        assertTrue(carritoService.estaVacio(session));
    }

    @Test
    void estaVacio_WithNonEmptyCarrito_ShouldReturnFalse() {
        // Arrange
        when(session.getAttribute("carrito")).thenReturn(carritoMap);

        // Act & Assert
        assertFalse(carritoService.estaVacio(session));
    }

    @Test
    void getCantidadItems_ShouldReturnTotalQuantity() {
        // Arrange
        when(session.getAttribute("carrito")).thenReturn(carritoMap);

        // Act
        int result = carritoService.getCantidadItems(session);

        // Assert
        assertEquals(3, result); // 2 + 1
    }
}
