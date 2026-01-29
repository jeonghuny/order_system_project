package com.ordersystem.order.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberLoginDto {
    @NotBlank(message = "이메일을 올바르게 입력해주세요")
    private String email;
    @NotBlank(message = "비밀번호를 올바르게 입력해주세요")
    private String password;
}
