package com.ordersystem.order.member.controller;

import com.ordersystem.order.member.common.domain.BaseTimeEntity;
import com.ordersystem.order.member.domain.Member;
import com.ordersystem.order.member.dto.MemberCreateDto;
import com.ordersystem.order.member.dto.MemberLoginDto;
import com.ordersystem.order.member.service.MemberService;
import jakarta.validation.Valid;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController extends BaseTimeEntity {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid  MemberCreateDto dto){
        memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }

    @PostMapping("/doLogin")
    public String login(@RequestBody @Valid MemberLoginDto dto){
        Member member = memberService.login(dto);
        String token = null;
        return token;
    }
    @GetMapping("/list")
    public void findAll(){

    }

    @GetMapping("/myinfo")
    public void myInfo(){

    }

    @GetMapping("/detail/{id}")
    public void findById(Long id){

    }
}
