package com.ordersystem.order.ordering.service;

import com.ordersystem.order.member.domain.Member;
import com.ordersystem.order.member.repository.MemberRepository;
import com.ordersystem.order.ordering.domain.OrderDetail;
import com.ordersystem.order.ordering.domain.Ordering;
import com.ordersystem.order.ordering.dto.OrderCreateDto;
import com.ordersystem.order.ordering.dto.OrderListDto;
import com.ordersystem.order.ordering.repository.OrderDetailRepository;
import com.ordersystem.order.ordering.repository.OrderingRepository;
import com.ordersystem.order.product.domain.Product;
import com.ordersystem.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, MemberRepository memberRepository) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    public Long create( List<OrderCreateDto> orderCreateDtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder().member(member).build();

        for (OrderCreateDto dto : orderCreateDtoList){
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetailList().add(orderDetail);
        }
        orderingRepository.save(ordering);
        return ordering.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> myorders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return orderingRepository.findAllByMember(member).stream().map(o->OrderListDto.fromEntity(o)).collect(Collectors.toList());
    }
}