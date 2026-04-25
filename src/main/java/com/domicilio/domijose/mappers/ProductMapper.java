package com.domicilio.domijose.mappers;

import com.domicilio.domijose.dto.ProductDTO;
import com.domicilio.domijose.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductDTO dto);

    ProductDTO toDTO(Product product);

    List<ProductDTO> toDTOList(List<Product> products);
}