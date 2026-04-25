package com.domicilio.domijose.mappers;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.dto.OrderItemDTO;
import com.domicilio.domijose.models.Order;
import com.domicilio.domijose.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userPhone", source = "user.phone")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "metodoPago", source = "metodoPago")
    @Mapping(target = "items", source = "items")
    OrderDTO toDto(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(OrderDTO dto);

    List<OrderDTO> toDtoList(List<Order> orders);

    // OrderItem mappings
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.imageUrl")
    OrderItemDTO toItemDto(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    OrderItem toEntity(OrderItemDTO dto);

    List<OrderItemDTO> toItemDtoList(List<OrderItem> orderItems);
}