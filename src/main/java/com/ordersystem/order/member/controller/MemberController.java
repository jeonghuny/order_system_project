package com.ordersystem.order.member.controller;

import com.ordersystem.order.member.common.auth.JwtTokenProvider;
import com.ordersystem.order.member.common.domain.BaseTimeEntity;
import com.ordersystem.order.member.domain.Member;
import com.ordersystem.order.member.dto.MemberCreateDto;
import com.ordersystem.order.member.dto.MemberDetailDto;
import com.ordersystem.order.member.dto.MemberListDto;
import com.ordersystem.order.member.dto.MemberLoginDto;
import com.ordersystem.order.member.service.MemberService;
import jakarta.validation.Valid;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController extends BaseTimeEntity {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid MemberCreateDto dto){
        Member member = memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(member.getId());
    }

    @PostMapping("/doLogin")
    public String login(@RequestBody @Valid MemberLoginDto dto){
        Member member = memberService.login(dto);
        String token = jwtTokenProvider.createToken(member);
        return token;
    }
    @GetMapping("/list")
    public List<MemberListDto> findAll(@AuthenticationPrincipal String principal){
        List<MemberListDto> memberListDto = memberService.findAll();
        return memberListDto;
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal String principal){
        MemberDetailDto dto = memberService.myInfo();
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/detail/{id}")
    public void findById(Long id){

    }
}
