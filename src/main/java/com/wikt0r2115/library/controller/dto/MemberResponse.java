package com.wikt0r2115.library.controller.dto;

import com.wikt0r2115.library.domain.Member;

public record MemberResponse(
        Long memberId,
        String firstName,
        String lastName,
        String email
) {
    public static MemberResponse from(Member member){
        return new MemberResponse(
                member.getMemberId(),
                member.getFirstName(),
                member.getLastName(),
                member.getEmail());
    }
}
