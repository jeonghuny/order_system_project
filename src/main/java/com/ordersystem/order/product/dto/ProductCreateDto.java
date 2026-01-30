package com.ordersystem.order.product.dto;

import com.ordersystem.order.member.domain.Member;
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
public class ProductCreateDto {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long price;
    private String category;
    @Column(nullable = false)
    private Long stockQuantity;

    public Product toEntity(Member member){
        return Product.builder()
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .stockQuantity(this.stockQuantity)
                .member(member)
                .build();
    }
}
