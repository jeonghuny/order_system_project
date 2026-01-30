package com.ordersystem.order.product.controller;

import com.ordersystem.order.product.dto.ProductCreateDto;
import com.ordersystem.order.product.dto.ProductDetailDto;
import com.ordersystem.order.product.dto.ProductSearchDto;
import com.ordersystem.order.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<?> findById(@PathVariable Long id){
        ProductDetailDto postDetailDto = productService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(postDetailDto);
    }

    @GetMapping("/list")
    public Page<ProductDetailDto> productDetailDtos(@PageableDefault(size=10 , sort="id", direction = Sort.Direction.DESC)
                                                    Pageable pageable, @ModelAttribute ProductSearchDto searchDto){
        return productService.findAll(pageable, searchDto);
    }
}
