package com.ordersystem.order.product.service;

import com.ordersystem.order.member.domain.Member;
import com.ordersystem.order.member.repository.MemberRepository;
import com.ordersystem.order.product.domain.Product;
import com.ordersystem.order.product.dto.ProductCreateDto;
import com.ordersystem.order.product.dto.ProductDetailDto;
import com.ordersystem.order.product.dto.ProductSearchDto;
import com.ordersystem.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

        Product product = dto.toEntity(member);
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

    public Page<ProductDetailDto> findAll(Pageable pageable, ProductSearchDto searchDto) {

        Specification<Product> specification = new Specification<Product>() {

            // 이안에 쿼리가 담겨있다고 보면 됨.
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();

//                root : 엔티티의 컬럼명을 접근하기 위한 객체, criteriaBuilder : 쿼리를 생성하기 위한 객체
                if (searchDto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%" + searchDto.getProductName() + "%"));
                }
                if (searchDto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateArr.length; i++) {
                    predicateArr[i] = predicateList.get(i);
                }
//                predicate에는 검색조건들이 담길것이고, 이 Predicate list를 한줄의 predicate로 조립.
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };

        Page<Product> productList = productRepository.findAll(specification, pageable);
//        Page객체 안에 Entity-> Dto로 쉽게 변환할수 있는 편의 제공 // 리스트도 아님.
        return productList.map(p->ProductDetailDto.fromEntity(p));
    }
}


