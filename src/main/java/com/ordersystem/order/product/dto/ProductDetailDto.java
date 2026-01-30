package com.ordersystem.order.product.dto;


import com.ordersystem.order.product.domain.Product;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDetailDto {
    private Long id;
    private String name;
    private String category;
    private Long price;
    private Long stockQuantity;
    private String imagePath;

    public static ProductDetailDto fromEntity(Product product){
        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imagePath(product.getImage_path())
                .build();
    }
}
