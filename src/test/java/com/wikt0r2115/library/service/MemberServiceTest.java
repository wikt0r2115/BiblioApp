package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.MemberResponse;
import com.wikt0r2115.library.domain.Member;
import com.wikt0r2115.library.infrastructure.LoanRepository;
import com.wikt0r2115.library.infrastructure.MemberRepository;
import com.wikt0r2115.library.service.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.wikt0r2115.library.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private LoanRepository loanRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, loanRepository);
    }

    @Test
    void createMember_whenDataIsValid_savesMemberAndReturnsResponse() {
        when(memberRepository.save(any(Member.class)))
                .thenReturn(sampleMemberWithId());
        MemberResponse response = memberService.createMember(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, MEMBER_EMAIL);

        assertAll(
                () -> assertEquals(MEMBER_ID, response.memberId()),
                () -> assertEquals(MEMBER_FIRST_NAME, response.firstName()),
                () -> assertEquals(MEMBER_LAST_NAME, response.lastName()),
                () -> assertEquals(MEMBER_EMAIL, response.email())
        );
        verify(memberRepository).save(argThat(member ->
                MEMBER_FIRST_NAME.equals(member.getFirstName())
                        && MEMBER_LAST_NAME.equals(member.getLastName())
                        && MEMBER_EMAIL.equals(member.getEmail())));
    }
    @Test
    void createMember_whenDataIsInvalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> memberService.createMember("","",""));
        verifyNoInteractions(memberRepository);
    }
    @Test
    void findByIdWhereArchivedFalse_whenMemberExists_returnsMemberResponse() {
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.of(sampleMemberWithId()));
        MemberResponse response = memberService.findByIdWhereArchivedFalse(MEMBER_ID);
        assertEquals(MEMBER_ID, response.memberId());
        assertEquals(MEMBER_EMAIL, response.email());
    }
    @Test
    void findByIdWhereArchivedFalse_whenMemberIsArchived_throwsMemberNotFoundException() {
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.empty());
        assertThrows(MemberNotFoundException.class, () -> memberService.findByIdWhereArchivedFalse(MEMBER_ID));
    }
    @Test
    void findById_whenMemberExists_returnsMember() {
        Member member = sampleMemberWithId();
        when(memberRepository.findById(MEMBER_ID))
                .thenReturn(Optional.of(member));

        assertSame(member, memberService.findById(MEMBER_ID));
    }
    @Test
    void findById_whenMemberDoesNotExist_throwsMemberNotFoundException() {
        when(memberRepository.findById(MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.findById(MEMBER_ID));
    }
    @Test
    void updateMember_whenMemberExists_updatesAndSavesMember() {
        Member existingMember = sampleMemberWithId();
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberResponse member = memberService.updateMember(
                MEMBER_ID,
                NEW_MEMBER_FIRST_NAME,
                NEW_MEMBER_LAST_NAME,
                NEW_MEMBER_EMAIL);

        assertEquals(NEW_MEMBER_FIRST_NAME, member.firstName());
        assertEquals(NEW_MEMBER_LAST_NAME, member.lastName());
        assertEquals(NEW_MEMBER_EMAIL, member.email());

        verify(memberRepository).save(existingMember);
    }

    @Test
    void updateMember_whenMemberDoesntExist_throwsMemberNotFoundException () {
        assertThrows(MemberNotFoundException.class, () -> memberService.updateMember(20L, NEW_MEMBER_FIRST_NAME,NEW_MEMBER_LAST_NAME,NEW_MEMBER_EMAIL));
        verify(memberRepository, never()).save(any(Member.class));
    }
    @Test
    void updateMember_whenDataIsInvalid_doesNotSaveMember() {
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.of(sampleMemberWithId()));

        assertThrows(IllegalArgumentException.class,
                () -> memberService.updateMember(MEMBER_ID, NEW_MEMBER_FIRST_NAME, NEW_MEMBER_LAST_NAME, "invalid"));
        verify(memberRepository, never()).save(any(Member.class));
    }
    @Test
    void deleteMember_whenMemberHasNoActiveLoans_archivesMember() {
        Member member = sampleMemberWithId();
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNull(member))
                .thenReturn(List.of());

        memberService.deleteMember(MEMBER_ID);

        assertTrue(member.isArchived());
        verify(memberRepository).save(member);
    }
    @Test
    void deleteMember_whenMemberHasActiveLoans_throwsIllegalStateException() {
        Member member = sampleMemberWithId();
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.of(member));
        when(loanRepository.findByMemberAndReturnedAtIsNull(member))
                .thenReturn(List.of(sampleActiveLoanWithIds()));

        assertThrows(IllegalStateException.class, () -> memberService.deleteMember(MEMBER_ID));
        assertFalse(member.isArchived());
        verify(memberRepository, never()).save(any(Member.class));
    }
    @Test
    void deleteMember_whenMemberIsArchived_throwsMemberNotFoundException() {
        when(memberRepository.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.deleteMember(MEMBER_ID));
        verifyNoInteractions(loanRepository);
        verify(memberRepository, never()).save(any(Member.class));
    }
}
