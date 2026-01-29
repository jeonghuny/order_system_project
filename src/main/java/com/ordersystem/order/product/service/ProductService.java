package com.ordersystem.order.product.service;

import com.ordersystem.order.product.domain.Product;
import com.ordersystem.order.product.dto.ProductCreateDto;
import com.ordersystem.order.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    public void save(ProductCreateDto dto, MultipartFile profileImage){
        Product product = dto.toEntity();
        Product productDb = productRepository.save(product);
    }
}
