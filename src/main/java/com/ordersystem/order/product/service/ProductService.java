package com.ordersystem.order.product.service;

import com.ordersystem.order.member.domain.Member;
import com.ordersystem.order.member.repository.MemberRepository;
import com.ordersystem.order.product.domain.Product;
import com.ordersystem.order.product.dto.ProductCreateDto;
import com.ordersystem.order.product.dto.ProductDetailDto;
import com.ordersystem.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final MemberRepository memberRepository;

    @Value("${aws.s3.bucket1}")
    private String bucket;

    @Autowired
    public ProductService(ProductRepository productRepository, S3Client s3Client, MemberRepository memberRepository) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.memberRepository = memberRepository;
    }
    public void save(ProductCreateDto dto, MultipartFile productImage){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 관리자가 판매자?
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("판매자 없음"));

        Product product = dto.toEntity(member, null);
        Product productDb = productRepository.save(product);

        if(productImage != null){
            String fileName = "product-"+product.getId()+"-productImage-"+productImage.getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productImage.getContentType()) //image/jpeg , video/mp4 ... 등
                    .build();

//        aws에 이미지 업로드(byte형태로)
            try {
                s3Client.putObject(request, RequestBody.fromBytes(productImage.getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//        aws의 이미지 url 추출
            String image_path = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateProductImageUrl(image_path);
        }
    }

    public ProductDetailDto findById(Long id){
        Optional<Product> optProduct = productRepository.findById(id);
        Product product = optProduct.orElseThrow(()->new EntityNotFoundException("상품아이디가 존재하지 않습니다."));
        ProductDetailDto dto = ProductDetailDto.fromEntity(product);
        return dto;
    }
}
