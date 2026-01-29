package com.ordersystem.order.product.repository;

import com.ordersystem.order.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
}
