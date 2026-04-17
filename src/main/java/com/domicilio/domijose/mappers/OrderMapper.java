package com.domicilio.domijose.mappers;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.models.Order;
import com.domicilio.domijose.models.OrderItem;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    
    @Mapping(target = "items", source = "items")
    OrderDTO toDTO(Order order);
    
    List<OrderDTO> toDTOList(List<Order> orders);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
    Order toEntity(OrderDTO dto);
}