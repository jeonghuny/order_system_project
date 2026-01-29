package com.ordersystem.order.member.service;
import com.ordersystem.order.member.domain.Member;
import com.ordersystem.order.member.dto.MemberCreateDto;
import com.ordersystem.order.member.dto.MemberLoginDto;
import com.ordersystem.order.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    public void save(MemberCreateDto dto){
        if(memberRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("entity is not found");
        }else{
            Member member = dto.toEntity("1234");
            memberRepository.save(member);
        }
    }

    public String login(MemberLoginDto dto){
        if(memberRepository.findByEmail(dto.get)
    }

    public void findAll(){

    }

    public void myInfo(){

    }

    public void findById(Long id){

    }

}
