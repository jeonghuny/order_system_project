package com.ordersystem.order.member.service;
import com.ordersystem.order.member.domain.Member;
import com.ordersystem.order.member.dto.MemberCreateDto;
import com.ordersystem.order.member.dto.MemberDetailDto;
import com.ordersystem.order.member.dto.MemberListDto;
import com.ordersystem.order.member.dto.MemberLoginDto;
import com.ordersystem.order.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public Member save(MemberCreateDto dto){
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("entity is not found");
        }
        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
        memberRepository.save(member);
        return member;
    }

    public Member login(MemberLoginDto dto){
        Optional<Member> optMember = memberRepository.findByEmail(dto.getEmail());
        boolean flag = true;
        if(!optMember.isPresent()){
            flag = false;
        }else{
            if(!passwordEncoder.matches(dto.getPassword(), optMember.get().getPassword())){
                flag = false;
            }
        }
        if(!flag){
            throw new IllegalArgumentException("이메일또는 비밀번호가 일치하지 않습니다.");
        }
        return optMember.get();
    }

    @Transactional(readOnly = true)
    public List<MemberListDto> findAll(){
        List<MemberListDto> memberListDtoList = memberRepository.findAll().stream().map(a->MemberListDto.fromEntity(a))
                .collect(Collectors.toList());
        return memberListDtoList;
    }

    @Transactional(readOnly = true)
    public MemberDetailDto myInfo(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Optional<Member> optMember = memberRepository.findByEmail(email);
        Member member = optMember.orElseThrow(()->new NoSuchElementException("entity is not found"));
        MemberDetailDto dto = MemberDetailDto.fromEntity(member);
        return dto;
    }

    @Transactional(readOnly = true)
    public MemberDetailDto findById(Long id){
        Optional<Member> optMember = memberRepository.findById(id);
        Member member = optMember.orElseThrow(()-> new NoSuchElementException("entity is not found"));
        MemberDetailDto dto = MemberDetailDto.fromEntity(member);
        return dto;
    }

}
