package com.domicilio.domijose.services;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.mappers.OrderMapper;
import com.domicilio.domijose.models.Order;
import com.domicilio.domijose.models.enums.OrderStatus;
import com.domicilio.domijose.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.debug("Obteniendo pedidos para usuario ID: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);
        log.debug("Pedidos encontrados: {}", orders.size());
        return orderMapper.toDTOList(orders);
    }

    public Optional<OrderDTO> getOrderByIdAndUserId(Long orderId, Long userId) {
        log.debug("Buscando pedido ID: {} para usuario ID: {}", orderId, userId);
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(orderMapper::toDTO);
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO, Long userId) {
        log.info("Creando nuevo pedido para usuario ID: {}", userId);
        
        Order order = new Order();
        order.setOrderDate(orderDTO.getOrderDate());
        order.setStatus(OrderStatus.PENDIENTE);
        order.setTotalAmount(orderDTO.getTotalAmount());
        
        Order saved = orderRepository.save(order);
        log.info("Pedido creado con ID: {}", saved.getId());
        return orderMapper.toDTO(saved);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        log.info("Cancelando pedido ID: {} para usuario ID: {}", orderId, userId);
        
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> {
                    log.error("Pedido no encontrado: {}", orderId);
                    return new IllegalArgumentException("Pedido no encontrado");
                });
        
        if (order.getStatus() != OrderStatus.PENDIENTE) {
            log.warn("No se puede cancelar pedido con estado: {}", order.getStatus());
            throw new IllegalStateException("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }
        
        order.setStatus(OrderStatus.CANCELADO);
        orderRepository.save(order);
        log.info("Pedido cancelado: {}", orderId);
    }
}