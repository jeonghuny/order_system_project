package com.ordersystem.order.product.domain;

import com.ordersystem.order.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.batch.infrastructure.item.validator.SpringValidator;

@AllArgsConstructor
@NoArgsConstructor
@Getter @ToString
@Builder
@Entity
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id" , foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;


    public void updateStockQuantity(Long stockQuantity){
        this.stockQuantity=stockQuantity;
    }
}
