package com.ordersystem.order.member.common.auth;


import com.ordersystem.order.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String st_secret_key;

    @Value("${jwt.expiration}")
    private int expiration;

    private Key secret_key;

    @PostConstruct
    public void init(){
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(Member member){

        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role", member.getRole().toString());

        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration*60*1000L))
                .signWith(secret_key)
                .compact();

        return token;
    }

}
