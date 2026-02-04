package com.ordersystem.order.ordering.service;

import com.ordersystem.order.common.service.RabbitmqStockService;
import com.ordersystem.order.common.service.SseAlarmService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String,String> redisTemplate;
    private final RabbitmqStockService rabbitmqStockService;

    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, MemberRepository memberRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, RabbitmqStockService rabbitmqStockService) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.rabbitmqStockService = rabbitmqStockService;
    }
//    동시성 제어방법 1. 특정 메서드에 한해 격리수준 올리기.
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long create( List<OrderCreateDto> orderCreateDtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder().member(member).build();

        for (OrderCreateDto dto : orderCreateDtoList){
//             동시성제어방법2. select for update를 통한 락 설정 이후 조회
//            Product product = productRepository.findByIdForUpdate(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
//            동시성제어방법3. redis에서 재고 수량 확인 및 재고수량 감소처리
            // 단점 : 조회와 감소요청이 분리되다 보니, 동시성문제 발생 -> 해결책 : 루아(lua)스크립트를 통해 여러작업을 단일요청으로 묶어 해결
            // 이 앞에서 줄서지만 밑에 자바코드로 들어갈땐 동시에 들어간다. 그래서 주문 수량 오류남
            // 이 부분에서 원자성깨지는 문제가 있다. redis 싱글스레드를 통해 이슈 제어하려고 하는데 루아스크립트 뽑아줘
            String remain = redisTemplate.opsForValue().get(String.valueOf(dto.getProductId()));
            int remainQuantity = Integer.parseInt(remain);
            if(remainQuantity < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }else{
                redisTemplate.opsForValue().decrement(String.valueOf(dto.getProductId()),dto.getProductCount());
            }
//            if(product.getStockQuantity() < dto.getProductCount()){
//                throw new IllegalArgumentException("재고가 부족합니다.");
//            }
//            product.updateStockQuantity(dto.getProductCount());
            OrderDetail orderDetail = OrderDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            ordering.getOrderDetailList().add(orderDetail);

//            rdb동기화를 위한 작업1 : 스케쥴러 활용
//            rdb동기화를 위한 작업2 : rabbitmq에 rdb 재고감소 메시지 발행
            rabbitmqStockService.publish(dto.getProductId(), dto.getProductCount());
        }
        orderingRepository.save(ordering);

//        주문성공시 admin 유저에게 알림메시지 전송
        String message = ordering.getId() + "번 주문이 들어왔습니다.";
        sseAlarmService.sendMessage("admin@naver.com", email, message);
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