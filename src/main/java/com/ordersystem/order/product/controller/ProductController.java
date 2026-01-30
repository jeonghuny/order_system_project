package com.ordersystem.order.product.controller;

import com.ordersystem.order.product.dto.ProductCreateDto;
import com.ordersystem.order.product.dto.ProductDetailDto;
import com.ordersystem.order.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/product")
public class ProductController {
    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestPart("product") @Valid ProductCreateDto dto,
                                    @RequestPart("productImage") MultipartFile productImage){
        productService.save(dto, productImage);
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }

    @GetMapping("/detail/{id}")
    public ProductDetailDto findById(@PathVariable Long id){
        productService.findById(id);

        return null;
    }
}
