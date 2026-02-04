package com.ordersystem.order.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ordersystem.order.common.dtos.RabbitMqStockDto;
import com.ordersystem.order.product.domain.Product;
import com.ordersystem.order.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitmqStockService {
    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public RabbitmqStockService(RabbitTemplate rabbitTemplate, ProductRepository productRepository, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(Long productId, int productCount){
        RabbitMqStockDto dto = RabbitMqStockDto.builder()
                .productId(productId)
                .productCount(productCount)
                .build();
        // 재고 변경 정보 dto로 만듬
        // 큐에다가 dto를 발행하겠다.(RabbitMQ 큐(stockQueue)로 메시지를 보냄)
        // convertAndSend : 객체를 자동으로 직렬화(JSON)
        rabbitTemplate.convertAndSend("stockQueue", dto);
    }

//    RabbitListener : rabbitmq에 특정 큐에 대해 subscribe하는 어노테이션
//    RabbitListener는 단일스레드로 메시지를 처리하므로, 동시성이슈발생x. 다만, 멀티서버환경에서는 문제발생할 수 있음.
    //이 메서드는 stockQueue에 메시지가 들어오면 자동 실행됨.
    @RabbitListener(queues = "stockQueue")
    @Transactional
    public void subscribe(Message message) throws JsonProcessingException {
        String messageBody = new String(message.getBody());
        RabbitMqStockDto dto = objectMapper.readValue(messageBody, RabbitMqStockDto.class);
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("entity is not found"));
        product.updateStockQuantity(dto.getProductCount());
    }
}
