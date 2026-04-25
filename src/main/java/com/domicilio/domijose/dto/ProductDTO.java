package com.domicilio.domijose.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    private String imageUrl;
    private boolean available = true;
    private String category;
    
    private MultipartFile imagenFile; // Para subir archivos desde formulario

    // Constructor sin imagenFile (para responses)
    public ProductDTO(Long id, String name, String description, BigDecimal price,
                      Integer stock, String imageUrl, boolean available, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.available = available;
        this.category = category;
    }
    
    // Método útil para saber si es nuevo producto
    public boolean isNew() {
        return id == null;
    }
}