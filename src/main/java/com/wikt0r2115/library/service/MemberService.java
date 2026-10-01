package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.MemberResponse;
import com.wikt0r2115.library.domain.Member;
import com.wikt0r2115.library.infrastructure.LoanRepository;
import com.wikt0r2115.library.infrastructure.MemberRepository;
import com.wikt0r2115.library.service.exception.MemberNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public MemberService(MemberRepository memberRepository,
                         LoanRepository loanRepository){
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    public MemberResponse createMember(String firstName, String lastName, String email){
        return MemberResponse.from(memberRepository.save(new Member(firstName,lastName,email)));
    }

    public MemberResponse findByIdWhereArchivedFalse(Long id){
        return MemberResponse.from(findActiveMember(id));
    }

    private Member findActiveMember(Long id){
        return memberRepository.findByIdWhereArchivedFalse(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    public Member findById(Long id){
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    public MemberResponse updateMember(Long id, String firstName, String lastName, String email){
        Member member = findActiveMember(id);
        member.updateDetails(firstName, lastName, email);
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void deleteMember(Long id){
        Member member = findActiveMember(id);
        if(!loanRepository.findByMemberAndReturnedAtIsNull(member).isEmpty())
            throw new IllegalStateException("Member has active loans");
        member.markArchived();
        memberRepository.save(member);
    }
}
