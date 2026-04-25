package com.domicilio.domijose.services;

import com.domicilio.domijose.data.DataProviderOrder;
import com.domicilio.domijose.data.DataProviderProduct;
import com.domicilio.domijose.data.DataProviderUser;
import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.dto.OrderItemDTO;
import com.domicilio.domijose.mappers.OrderMapper;
import com.domicilio.domijose.models.Order;
import com.domicilio.domijose.models.OrderItem;
import com.domicilio.domijose.models.Product;
import com.domicilio.domijose.models.User;
import com.domicilio.domijose.models.enums.OrderStatus;
import com.domicilio.domijose.repositories.OrderRepository;
import com.domicilio.domijose.repositories.ProductRepository;
import com.domicilio.domijose.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private OrderDTO testOrderDTO;
    private OrderItemDTO testOrderItemDTO;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testUser = DataProviderUser.createUser();
        testProduct = DataProviderProduct.createProduct();
        testOrder = DataProviderOrder.createOrder();
        testOrderDTO = DataProviderOrder.createOrderDTO();
        testOrderItemDTO = DataProviderOrder.createOrderItemDTO();
        testOrderItem = DataProviderOrder.createOrderItem();

        // Set up relationships
        testOrder.setUser(testUser);
        testOrderItem.setProduct(testProduct);
        testOrder.getItems().add(testOrderItem);
    }

    @Test
    void getOrdersByUserId_ShouldReturnOrderList() {
        // Arrange
        List<Order> orders = DataProviderOrder.createOrderList();
        when(orderRepository.findByUserIdOrderByOrderDateDesc(anyLong())).thenReturn(orders);
        when(orderMapper.toDtoList(orders)).thenReturn(List.of(testOrderDTO, testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getOrdersByUserId(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findByUserIdOrderByOrderDateDesc(testUser.getId());
        verify(orderMapper).toDtoList(orders);
    }

    @Test
    void getOrderByIdAndUserId_WhenOrderExists_ShouldReturnOrderDTO() {
        // Arrange
        when(orderRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDto(testOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.getOrderByIdAndUserId(testOrder.getId(), testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO.getId(), result.getId());
        verify(orderRepository).findByIdAndUserId(testOrder.getId(), testUser.getId());
        verify(orderMapper).toDto(testOrder);
    }

    @Test
    void getOrderByIdAndUserId_WhenOrderNotExists_ShouldThrowException() {
        // Arrange
        when(orderRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.getOrderByIdAndUserId(1L, testUser.getId());
        });

        assertEquals("Pedido no encontrado", exception.getMessage());
        verify(orderRepository).findByIdAndUserId(1L, testUser.getId());
    }

    @Test
    void createOrder_WithValidData_ShouldCreateOrderSuccessfully() throws Exception {
        // Arrange
        List<OrderItemDTO> itemsDTO = DataProviderOrder.createOrderItemDTOList();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        
        // Mock the saved order to return the testOrder with an ID
        Order savedOrder = testOrder;
        savedOrder.setId(1L); // Set ID as if it was saved
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.createOrder(testOrderDTO, testUser.getId(), itemsDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(productRepository, times(itemsDTO.size())).findById(anyLong());
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(savedOrder);
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        List<OrderItemDTO> itemsDTO = DataProviderOrder.createOrderItemDTOList();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        
        // Mock product with insufficient stock
        Product lowStockProduct = DataProviderProduct.createProductWithStock(0); // 0 stock
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(lowStockProduct));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(testOrderDTO, testUser.getId(), itemsDTO);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(userRepository).findById(testUser.getId());
        verify(productRepository).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WithPendingOrder_ShouldCancelSuccessfully() {
        // Arrange
        when(orderRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(testOrder));
        // Note: Removed unnecessary stubbing of productRepository.findById
        // The service gets products from order items, not by calling findById

        // Act
        orderService.cancelOrder(testOrder.getId(), testUser.getId());

        // Assert
        verify(orderRepository).findByIdAndUserId(testOrder.getId(), testUser.getId());
        // Note: The service gets products from order items, not by calling findById
        verify(productRepository, times(testOrder.getItems().size())).save(any(Product.class));
        verify(orderRepository).save(testOrder);
    }

    @Test
    void cancelOrder_WithNonPendingOrder_ShouldThrowException() {
        // Arrange
        Order deliveredOrder = DataProviderOrder.createOrderWithStatus(OrderStatus.ENTREGADO);
        deliveredOrder.setUser(testUser);
        when(orderRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(deliveredOrder));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(deliveredOrder.getId(), testUser.getId());
        });

        assertEquals("Solo se pueden cancelar pedidos en estado PENDIENTE", exception.getMessage());
        verify(orderRepository).findByIdAndUserId(deliveredOrder.getId(), testUser.getId());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_ToDelivered_ShouldUpdateSuccessfully() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        // Act
        orderService.updateOrderStatus(testOrder.getId(), OrderStatus.ENTREGADO);

        // Assert
        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository).save(testOrder);
        // Verify that the deliver() method was called on the order
        assertEquals(OrderStatus.ENTREGADO, testOrder.getStatus());
    }

    @Test
    void updateOrderStatus_ToCancelled_ShouldUpdateSuccessfully() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        // Act
        orderService.updateOrderStatus(testOrder.getId(), OrderStatus.CANCELADO);

        // Assert
        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository).save(testOrder);
        // Verify that the cancel() method was called on the order
        assertEquals(OrderStatus.CANCELADO, testOrder.getStatus());
    }

    @Test
    void updateOrderStatus_ToPending_ShouldUpdateSuccessfully() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        // Act
        orderService.updateOrderStatus(testOrder.getId(), OrderStatus.PENDIENTE);

        // Assert
        verify(orderRepository).findById(testOrder.getId());
        verify(orderRepository).save(testOrder);
        // Verify that the status was set directly
        assertEquals(OrderStatus.PENDIENTE, testOrder.getStatus());
    }
}