package com.domicilio.domijose.repositories;

import com.domicilio.domijose.models.Order;
import com.domicilio.domijose.models.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByUserIdAndStatusNotOrderByOrderDateDesc(Long userId, OrderStatus status);

    List<Order> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, OrderStatus status);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    List<Order> findByUserIdAndOrderDateBetweenAndStatusNotOrderByOrderDateDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);

    long countByStatus(OrderStatus status);

    Page<Order> findByOrderDateBetweenOrderByOrderDateDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}