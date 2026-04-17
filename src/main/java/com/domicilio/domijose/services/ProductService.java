package com.domicilio.domijose.services;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.mappers.ProductMapper;
import com.domicilio.domijose.models.Product;
import com.domicilio.domijose.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileService fileService;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, FileService fileService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.fileService = fileService;
    }

    public List<ProductDTO> getAllAvailableProducts() {
        log.debug("Obteniendo todos los productos disponibles");
        List<Product> products = productRepository.findAllByAvailableTrue();
        List<ProductDTO> result = productMapper.toDTOList(products);
        log.debug("Productos obtenidos: {}", result.size());
        return result;
    }

    public Optional<ProductDTO> getProductById(Long id) {
        log.debug("Buscando producto por ID: {}", id);
        return productRepository.findByIdAndAvailableTrue(id)
                .map(productMapper::toDTO);
    }

    public ProductDTO saveProduct(ProductDTO dto) {
        log.info("Creando nuevo producto: {}", dto.getName());

        if (productRepository.existsByNameIgnoreCase(dto.getName())) {
            log.warn("Ya existe un producto con el nombre: {}", dto.getName());
            throw new IllegalArgumentException("Ya existe un producto con ese nombre");
        }

        String imageUrl = null;
        if (dto.getImagenFile() != null && !dto.getImagenFile().isEmpty()) {
            imageUrl = fileService.saveImage(dto.getImagenFile());
            log.info("Imagen guardada para producto: {}", imageUrl);
        }

        Product product = productMapper.toEntity(dto);
        product.setImageUrl(imageUrl);
        product.setAvailable(true);
        Product saved = productRepository.save(product);
        log.info("Producto creado con ID: {}", saved.getId());
        return productMapper.toDTO(saved);
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        log.info("Actualizando producto con ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Producto no encontrado: {}", id);
                    return new IllegalArgumentException("Producto no encontrado");
                });

        if (productRepository.existsByNameIgnoreCase(dto.getName()) &&
                !product.getName().equalsIgnoreCase(dto.getName())) {
            log.warn("Ya existe otro producto con el nombre: {}", dto.getName());
            throw new IllegalArgumentException("Ya existe otro producto con ese nombre");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setAvailable(dto.isAvailable());

        if (dto.getImagenFile() != null && !dto.getImagenFile().isEmpty()) {
            String imageUrl = fileService.saveImage(dto.getImagenFile());
            product.setImageUrl(imageUrl);
            log.info("Imagen actualizada para producto: {}", imageUrl);
        }

        Product saved = productRepository.save(product);
        log.info("Producto actualizado: {}", saved.getId());
        return productMapper.toDTO(saved);
    }

    public void deleteProduct(Long id) {
        log.info("Eliminando producto con ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Producto no encontrado para eliminar: {}", id);
                    return new IllegalArgumentException("Producto no encontrado");
                });

        product.setAvailable(false);
        productRepository.save(product);
        log.info("Producto marcado como no disponible: {}", id);
    }

    public List<ProductDTO> searchProducts(String keyword) {
        log.debug("Buscando productos por nombre: {}", keyword);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);
        return productMapper.toDTOList(products);
    }

    public List<ProductDTO> getAllProducts() {
        log.debug("Obteniendo todos los productos (incluye inactivos)");
        List<Product> products = productRepository.findAll();
        List<ProductDTO> result = productMapper.toDTOList(products);
        log.debug("Total productos: {}", result.size());
        return result;
    }

    public Optional<ProductDTO> getProductByIdForAdmin(Long id) {
        log.debug("Buscando producto por ID para admin: {}", id);
        return productRepository.findById(id)
                .map(productMapper::toDTO);
    }

    public void activateProduct(Long id) {
        log.info("Activando producto con ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Producto no encontrado para activar: {}", id);
                    return new IllegalArgumentException("Producto no encontrado");
                });
        product.setAvailable(true);
        productRepository.save(product);
        log.info("Producto activado: {}", id);
    }
}