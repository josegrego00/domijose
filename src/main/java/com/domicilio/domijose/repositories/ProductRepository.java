package com.domicilio.domijose.repositories;

import com.domicilio.domijose.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Método para validar nombre único (Regla #6)
    boolean existsByNameIgnoreCase(String name);
    
    // Método para validar nombre único excluyendo un ID (para actualizaciones)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE LOWER(p.name) = LOWER(:name) AND p.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);
    
    // Búsqueda paginada por categoría
    Page<Product> findByAvailableTrueAndCategoryIgnoreCase(String category, Pageable pageable);
    
    // Listar categorías disponibles
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.available = true")
    List<String> findDistinctCategories();
}