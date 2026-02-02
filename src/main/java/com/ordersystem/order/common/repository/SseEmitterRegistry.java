package com.ordersystem.order.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//사용자별 SSE 연결 객체를 관리하는 레지스트리(저장소).
@Component
public class SseEmitterRegistry {
//    SseEmitter객체는 사용자의 연결정보(ip, macaddress 등)을 의미
//    ConcurrentHashMap은 Thread-Safe한 map(동시성 이슈 발생 x)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    public void addSseEmitter(String email, SseEmitter sseEmitter){
        this.emitterMap.put(email, sseEmitter);
    }

    public SseEmitter getEmitter(String email){
        return this.emitterMap.get(email);
    }
}
