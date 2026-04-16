package com.domicilio.domijose.repositories;

import com.domicilio.domijose.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByAvailableTrue();
    
    List<Product> findAllByAvailableTrueOrderByCreatedAtDesc();
    
    Optional<Product> findByIdAndAvailableTrue(Long id);
    
    List<Product> findByNameContainingIgnoreCase(String keyword);
    
    Page<Product> findAllByAvailableTrue(Pageable pageable);
    
    boolean existsByNameIgnoreCase(String name);
}