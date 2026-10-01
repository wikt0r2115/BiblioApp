package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.controller.dto.CreateMemberRequest;
import com.wikt0r2115.library.controller.dto.MemberResponse;
import com.wikt0r2115.library.controller.dto.UpdateMemberRequest;
import com.wikt0r2115.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @GetMapping("/{id}")
    public MemberResponse findMember(@PathVariable Long id){
        return memberService.findByIdWhereArchivedFalse(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createNewMember(@Valid @RequestBody CreateMemberRequest request){
        return memberService.createMember(
                request.firstName(),
                request.lastName(),
                request.email()
        );
    }

    @PutMapping("/{id}")
    public MemberResponse updateDetails(@PathVariable Long id, @Valid @RequestBody UpdateMemberRequest request){
        return memberService.updateMember(
                id,
                request.firstName(),
                request.lastName(),
                request.email()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable Long id){
        memberService.deleteMember(id);
    }
}
