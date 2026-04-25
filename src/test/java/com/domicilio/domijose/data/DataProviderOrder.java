package com.domicilio.domijose.data;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.dto.OrderItemDTO;
import com.domicilio.domijose.models.Order;
import com.domicilio.domijose.models.OrderItem;
import com.domicilio.domijose.models.Product;
import com.domicilio.domijose.models.User;
import com.domicilio.domijose.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data provider for Order and OrderItem test data
 */
public class DataProviderOrder {

    public static Order createOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.PENDIENTE);
        return order;
    }

    public static Order createOrderWithStatus(OrderStatus status) {
        Order order = createOrder();
        order.setStatus(status);
        return order;
    }

    public static Order createOrderWithUser(User user) {
        Order order = createOrder();
        order.setUser(user);
        return order;
    }

    public static OrderDTO createOrderDTO() {
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setTotalAmount(new BigDecimal("99.99"));
        dto.setStatus(OrderStatus.PENDIENTE);
        return dto;
    }

    public static List<Order> createOrderList() {
        List<Order> orders = new ArrayList<>();
        orders.add(createOrder());
        
        Order order2 = new Order();
        order2.setId(2L);
        order2.setTotalAmount(new BigDecimal("149.99"));
        order2.setStatus(OrderStatus.ENTREGADO);
        orders.add(order2);
        
        return orders;
    }

    public static OrderItem createOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));
        item.setSubtotal(new BigDecimal("50.00"));
        return item;
    }

    public static OrderItem createOrderItemWithQuantity(int quantity) {
        OrderItem item = createOrderItem();
        item.setQuantity(quantity);
        item.setSubtotal(item.getUnitPrice().multiply(new BigDecimal(quantity)));
        return item;
    }

    public static OrderItemDTO createOrderItemDTO() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(1L);
        dto.setProductId(1L);
        dto.setQuantity(2);
        return dto;
    }

    public static List<OrderItem> createOrderItemList() {
        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem());
        
        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("30.00"));
        item2.setSubtotal(new BigDecimal("30.00"));
        items.add(item2);
        
        return items;
    }

    public static List<OrderItemDTO> createOrderItemDTOList() {
        List<OrderItemDTO> dtos = new ArrayList<>();
        dtos.add(createOrderItemDTO());
        
        OrderItemDTO dto2 = new OrderItemDTO();
        dto2.setId(2L);
        dto2.setProductId(2L);
        dto2.setQuantity(1);
        dtos.add(dto2);
        
        return dtos;
    }
}