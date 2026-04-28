package com.domicilio.domijose.services;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WhatsAppLinkService whatsAppLinkService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
            UserRepository userRepository, ProductRepository productRepository,
            WhatsAppLinkService whatsAppLinkService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.whatsAppLinkService = whatsAppLinkService;
        this.productService = productService;
    }

    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.debug("Obteniendo pedidos para usuario ID: {}", userId);
        List<Order> orders = orderRepository.findByUserIdAndStatusNotOrderByOrderDateDesc(userId,
                OrderStatus.CANCELADO);
        log.debug("Pedidos encontrados (sin cancelar): {}", orders.size());
        return orderMapper.toDtoList(orders);
    }

    public List<OrderDTO> getOrdersByUserIdAndDateRange(Long userId, LocalDate fecha) {
        LocalDateTime startOfDay = fecha.atStartOfDay();
        LocalDateTime endOfDay = fecha.atTime(LocalTime.MAX);
        log.debug("Obteniendo pedidos para usuario ID: {} en fecha: {}", userId, fecha);
        List<Order> orders = orderRepository.findByUserIdAndOrderDateBetweenAndStatusNotOrderByOrderDateDesc(
                userId, startOfDay, endOfDay, OrderStatus.CANCELADO);
        log.debug("Pedidos encontrados para fecha {}: {}", fecha, orders.size());
        return orderMapper.toDtoList(orders);
    }

    public OrderDTO getOrderByIdAndUserId(Long orderId, Long userId) {
        log.debug("Buscando pedido ID: {} para usuario ID: {}", orderId, userId);
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(orderMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Pedido no encontrado: {}", orderId);
                    return new IllegalArgumentException("Pedido no encontrado");
                });
    }

    public OrderDTO getOrderById(Long orderId) {
        log.debug("Buscando pedido ID: {}", orderId);
        return orderRepository.findById(orderId)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    public List<OrderDTO> getAllOrders() {
        log.debug("Obteniendo todos los pedidos");
        return orderMapper.toDtoList(orderRepository.findAll());
    }

    public Page<OrderDTO> getOrdersByDateWithPagination(LocalDate fecha, Pageable pageable) {
        LocalDateTime startOfDay = fecha.atStartOfDay();
        LocalDateTime endOfDay = fecha.atTime(LocalTime.MAX);
        log.debug("Obteniendo pedidos para fecha: {} con paginación", fecha);
        Page<Order> ordersPage = orderRepository.findByOrderDateBetweenOrderByOrderDateDesc(startOfDay, endOfDay, pageable);
        return ordersPage.map(orderMapper::toDto);
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO, Long userId, List<OrderItemDTO> itemsDTO) {
        log.info("Creando nuevo pedido para usuario ID: {}", userId);

        // Obtener usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Crear pedido
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.POR_CONFIRMAR);

        // Crear items del pedido y validar stock
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDTO itemDTO : itemsDTO) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Producto no encontrado: " + itemDTO.getProductId()));

            // Validar stock
            if (!productService.hasStock(product, itemDTO.getQuantity())) {
                throw new IllegalStateException("Stock insuficiente para: " + product.getName());
            }

            // Reducir stock
            productService.reduceStock(product, itemDTO.getQuantity());

            // Crear OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(product.getPrice()); // Snapshot del precio actual
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            order.getItems().add(orderItem);
            orderItem.setOrder(order);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        order.setMetodoPago(orderDTO != null ? orderDTO.getMetodoPago() : "EFECTIVO");

        Order saved = orderRepository.save(order);
        log.info("Pedido creado con ID: {}, Total: {}", saved.getId(), totalAmount);

        return orderMapper.toDto(saved);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        log.info("Cancelando pedido ID: {} para usuario ID: {}", orderId, userId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> {
                    log.error("Pedido no encontrado: {}", orderId);
                    return new IllegalArgumentException("Pedido no encontrado");
                });

        // Regla: Solo cancelar si está POR_CONFIRMAR
        if (order.getStatus() != OrderStatus.POR_CONFIRMAR) {
            log.warn("No se puede cancelar pedido con estado: {}", order.getStatus());
            throw new IllegalStateException("Solo se pueden cancelar pedidos por confirmar");
        }

        // Restaurar stock de los productos
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            productService.increaseStock(product, item.getQuantity());
            log.debug("Stock restaurado para producto: {} (+{})", product.getName(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELADO);
        orderRepository.save(order);
        log.info("Pedido cancelado: {}", orderId);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Actualizando estado de pedido ID: {} a {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (newStatus == OrderStatus.ENTREGADO) {
            if (order.getStatus() != OrderStatus.EN_CAMINO) {
                throw new IllegalStateException("Solo se pueden entregar pedidos en camino");
            }
            order.setStatus(OrderStatus.ENTREGADO);
        } else if (newStatus == OrderStatus.CANCELADO) {
            if (order.getStatus() != OrderStatus.POR_CONFIRMAR) {
                throw new IllegalStateException("Solo se pueden cancelar pedidos por confirmar");
            }
            order.setStatus(OrderStatus.CANCELADO);
        } else {
            order.setStatus(newStatus);
        }

        orderRepository.save(order);
        log.info("Estado actualizado para pedido: {}", orderId);
    }

    @Transactional
    public OrderDTO confirmOrder(Long orderId) {
        log.info("Confirmando pedido ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (order.getStatus() != OrderStatus.POR_CONFIRMAR) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos por confirmar");
        }

        order.setStatus(OrderStatus.CONFIRMADO);
        orderRepository.save(order);

        log.info("Pedido confirmado: {}", orderId);
        return orderMapper.toDto(order);
    }
}