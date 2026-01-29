package com.ordersystem.order.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.batch.infrastructure.item.validator.SpringValidator;

@AllArgsConstructor
@NoArgsConstructor
@Getter @ToString
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String category;
    @Column(nullable = false)
    private Long price;
    @Column(nullable = false)
    private Long stockQuantity;
    private String productImage;


}
