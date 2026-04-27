package com.domicilio.domijose.services;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.mappers.ProductMapper;
import com.domicilio.domijose.models.Product;
import com.domicilio.domijose.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private MockMultipartFile testImageFile;

    @BeforeEach
    void setUp() {
        testProduct = createTestProduct();
        testProductDTO = createTestProductDTO();
        testImageFile = new MockMultipartFile("image.jpg", "image.jpg", "image/jpeg", "image-content".getBytes());
    }

    private Product createTestProduct() {
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

    private ProductDTO createTestProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setName("Test Product");
        dto.setDescription("Test Description");
        dto.setPrice(new BigDecimal("25.00"));
        dto.setStock(10);
        dto.setAvailable(true);
        dto.setCategory("PLATO_FUERTE");
        return dto;
    }

    // ========== MÉTODOS PÚBLICOS (CLIENTE) ==========

    @Test
    void getAllAvailableProducts_ShouldReturnAvailableProducts() {
        // Arrange
        List<Product> products = List.of(createTestProduct());
        when(productRepository.findAllByAvailableTrue()).thenReturn(products);
        when(productMapper.toDTOList(products)).thenReturn(List.of(testProductDTO));

        // Act
        List<ProductDTO> result = productService.getAllAvailableProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findAllByAvailableTrue();
        verify(productMapper).toDTOList(products);
    }

    @Test
    void getProductByIdForPublic_WhenProductExists_ShouldReturnProductDTO() {
        // Arrange
        when(productRepository.findByIdAndAvailableTrue(anyLong())).thenReturn(Optional.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        // Act
        ProductDTO result = productService.getProductByIdForPublic(testProduct.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testProductDTO.getId(), result.getId());
        verify(productRepository).findByIdAndAvailableTrue(testProduct.getId());
        verify(productMapper).toDTO(testProduct);
    }

    @Test
    void getProductByIdForPublic_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findByIdAndAvailableTrue(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductByIdForPublic(99L);
        });

        assertEquals("Producto no disponible", exception.getMessage());
        verify(productRepository).findByIdAndAvailableTrue(99L);
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        // Arrange
        String keyword = "Test";
        List<Product> products = List.of(createTestProduct());
        when(productRepository.findByNameContainingIgnoreCase(keyword)).thenReturn(products);
        when(productMapper.toDTOList(products)).thenReturn(List.of(testProductDTO));

        // Act
        List<ProductDTO> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByNameContainingIgnoreCase(keyword);
    }

    // ========== MÉTODOS DE ADMINISTRACIÓN (ADMIN) ==========

    @Test
    void createProduct_WithValidData_ShouldCreateProductSuccessfully() {
        // Arrange
        when(fileService.saveImage(any())).thenReturn("images/productos/test-image.jpg");
        when(productRepository.existsByNameIgnoreCase(testProductDTO.getName())).thenReturn(false);
        when(productMapper.toEntity(testProductDTO)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        // Act
        ProductDTO result = productService.createProduct(testProductDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testProductDTO.getName(), result.getName());
        assertTrue(result.isAvailable());
        verify(productRepository).existsByNameIgnoreCase(testProductDTO.getName());
        verify(fileService).saveImage(testProductDTO.getImagenFile());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_WithExistingName_ShouldThrowException() {
        // Arrange
        when(productRepository.existsByNameIgnoreCase(testProductDTO.getName())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(testProductDTO);
        });

        assertEquals("Ya existe un producto con ese nombre", exception.getMessage());
        verify(productRepository).existsByNameIgnoreCase(testProductDTO.getName());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_WithValidData_ShouldUpdateProductSuccessfully() {
        // Arrange
        ProductDTO updatedDTO = createTestProductDTO();
        updatedDTO.setName("Updated Product");
        updatedDTO.setPrice(new BigDecimal("30.00"));

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByNameIgnoreCase(updatedDTO.getName())).thenReturn(false);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(updatedDTO);

        // Act
        ProductDTO result = productService.updateProduct(testProduct.getId(), updatedDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(new BigDecimal("30.00"), result.getPrice());
        verify(productRepository).findById(testProduct.getId());
        verify(productRepository).save(testProduct);
    }

    @Test
    void updateProduct_WithDifferentExistingName_ShouldThrowException() {
        // Arrange
        ProductDTO updatedDTO = createTestProductDTO();
        updatedDTO.setName("Another Product");

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByNameIgnoreCase(updatedDTO.getName())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(testProduct.getId(), updatedDTO);
        });

        assertEquals("Ya existe otro producto con ese nombre", exception.getMessage());
        verify(productRepository).findById(testProduct.getId());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        ProductDTO updatedDTO = createTestProductDTO();
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(99L, updatedDTO);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productRepository).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WithValidId_ShouldMarkProductAsNotAvailable() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        // Act
        productService.deleteProduct(testProduct.getId());

        // Assert
        assertFalse(testProduct.isAvailable());
        verify(productRepository).findById(testProduct.getId());
        verify(productRepository).save(testProduct);
    }

    @Test
    void deleteProduct_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProduct(99L);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productRepository).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void activateProduct_WithValidId_ShouldActivateProduct() {
        // Arrange
        testProduct.setAvailable(false);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        // Act
        productService.activateProduct(testProduct.getId());

        // Assert
        assertTrue(testProduct.isAvailable());
        verify(productRepository).findById(testProduct.getId());
        verify(productRepository).save(testProduct);
    }

    @Test
    void activateProduct_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.activateProduct(99L);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productRepository).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProductsForAdmin_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = List.of(createTestProduct());
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDTOList(products)).thenReturn(List.of(testProductDTO));

        // Act
        List<ProductDTO> result = productService.getAllProductsForAdmin();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void getProductByIdForAdmin_WhenProductExists_ShouldReturnProductDTO() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        // Act
        ProductDTO result = productService.getProductByIdForAdmin(testProduct.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testProductDTO.getId(), result.getId());
        verify(productRepository).findById(testProduct.getId());
    }

    @Test
    void getProductByIdForAdmin_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductByIdForAdmin(99L);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productRepository).findById(99L);
    }
}
