package com.domicilio.domijose.data;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.models.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data provider for Product test data
 */
public class DataProviderProduct {

    public static Product createProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("25.00"));
        product.setStock(10);
        product.setImageUrl("test-image.jpg");
        product.setAvailable(true);
        product.setCategory("PLATO_FUERTE");
        return product;
    }

    public static Product createProductWithStock(int stock) {
        Product product = createProduct();
        product.setStock(stock);
        return product;
    }

    public static ProductDTO createProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setName("Test Product");
        dto.setDescription("Test Description");
        dto.setPrice(new BigDecimal("25.00"));
        dto.setStock(10);
        dto.setAvailable(true);
        dto.setCategory("PLATO_FUERTE");
        // Note: imagenFile is not set as it's a MultipartFile
        return dto;
    }

    public static List<Product> createProductList() {
        List<Product> products = new ArrayList<>();
        products.add(createProduct());
        
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Test Product 2");
        product2.setDescription("Test Description 2");
        product2.setPrice(new BigDecimal("30.00"));
        product2.setStock(5);
        product2.setImageUrl("test-image2.jpg");
        product2.setAvailable(false);
        product2.setCategory("POSTRE");
        products.add(product2);
        
        return products;
    }

    public static Product createProductWithName(String name) {
        Product product = createProduct();
        product.setName(name);
        return product;
    }

    public static ProductDTO createProductDTO2() {
        ProductDTO dto = new ProductDTO();
        dto.setId(2L);
        dto.setName("Test Product 2");
        dto.setDescription("Test Description 2");
        dto.setPrice(new BigDecimal("30.00"));
        dto.setStock(5);
        dto.setAvailable(true);
        dto.setCategory("POSTRE");
        return dto;
    }
}