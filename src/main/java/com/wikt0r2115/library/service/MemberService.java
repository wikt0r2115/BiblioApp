package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Member;
import com.wikt0r2115.library.infrastructure.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    public Member createMember(String firstName, String lastName, String email){
        return memberRepository.save(new Member(firstName,lastName,email));
    }

    public Member findById(Long id){
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    public Member updateMember(Long id, String firstName, String lastName, String email){
        Member member = findById(id);
        member.updateDetails(firstName, lastName, email);
        return memberRepository.save(member);
    }

    public void deleteMember(Long id){
        Member member = findById(id);
        memberRepository.delete(member);
    }
}
