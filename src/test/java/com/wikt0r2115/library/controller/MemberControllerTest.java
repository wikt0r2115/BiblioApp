package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.controller.dto.MemberResponse;
import com.wikt0r2115.library.service.MemberService;
import com.wikt0r2115.library.service.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static com.wikt0r2115.library.TestData.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {
    @Mock
    private MemberService memberService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new MemberController(memberService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void findMember_whenMemberExists_returnsMember() throws Exception {
        when(memberService.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenReturn(MemberResponse.from(sampleMemberWithId()));

        mockMvc.perform(get("/member/{id}", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
                .andExpect(jsonPath("$.firstName").value(MEMBER_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(MEMBER_LAST_NAME))
                .andExpect(jsonPath("$.email").value(MEMBER_EMAIL));
    }

    @Test
    void findMember_whenMemberDoesNotExist_returnsNotFound() throws Exception {
        when(memberService.findByIdWhereArchivedFalse(MEMBER_ID))
                .thenThrow(new MemberNotFoundException(MEMBER_ID));

        mockMvc.perform(get("/member/{id}", MEMBER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"))
                .andExpect(jsonPath("$.detail").value("Member does not exist"));
    }

    @Test
    void createMember_whenRequestIsValid_returnsCreatedMember() throws Exception {
        when(memberService.createMember(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, MEMBER_EMAIL))
                .thenReturn(MemberResponse.from(sampleMemberWithId()));

        mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Jan","lastName":"Kowalski","email":"jan@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
                .andExpect(jsonPath("$.firstName").value(MEMBER_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(MEMBER_LAST_NAME))
                .andExpect(jsonPath("$.email").value(MEMBER_EMAIL));

        verify(memberService).createMember(MEMBER_FIRST_NAME, MEMBER_LAST_NAME, MEMBER_EMAIL);
    }

    @Test
    void createMember_whenRequestIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Kowalski","email":"invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        verifyNoInteractions(memberService);
    }

    @Test
    void updateMember_whenRequestIsValid_returnsUpdatedMember() throws Exception {
        MemberResponse updated = new MemberResponse(MEMBER_ID, NEW_MEMBER_FIRST_NAME,
                NEW_MEMBER_LAST_NAME, NEW_MEMBER_EMAIL);
        when(memberService.updateMember(MEMBER_ID, NEW_MEMBER_FIRST_NAME, NEW_MEMBER_LAST_NAME, NEW_MEMBER_EMAIL))
                .thenReturn(updated);

        mockMvc.perform(put("/member/{id}", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Kuba","lastName":"Kowal","email":"kuba@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(MEMBER_ID))
                .andExpect(jsonPath("$.firstName").value(NEW_MEMBER_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(NEW_MEMBER_LAST_NAME))
                .andExpect(jsonPath("$.email").value(NEW_MEMBER_EMAIL));

        verify(memberService).updateMember(MEMBER_ID, NEW_MEMBER_FIRST_NAME, NEW_MEMBER_LAST_NAME, NEW_MEMBER_EMAIL);
    }

    @Test
    void updateMember_whenRequestIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/member/{id}", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Kuba","lastName":"","email":"invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        verifyNoInteractions(memberService);
    }

    @Test
    void updateMember_whenMemberDoesNotExist_returnsNotFound() throws Exception {
        when(memberService.updateMember(MEMBER_ID, NEW_MEMBER_FIRST_NAME, NEW_MEMBER_LAST_NAME, NEW_MEMBER_EMAIL))
                .thenThrow(new MemberNotFoundException(MEMBER_ID));

        mockMvc.perform(put("/member/{id}", MEMBER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Kuba","lastName":"Kowal","email":"kuba@example.com"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"));
    }

    @Test
    void deleteMember_whenMemberHasNoActiveLoans_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/member/{id}", MEMBER_ID))
                .andExpect(status().isNoContent());

        verify(memberService).deleteMember(MEMBER_ID);
    }

    @Test
    void deleteMember_whenMemberDoesNotExist_returnsNotFound() throws Exception {
        doThrow(new MemberNotFoundException(MEMBER_ID))
                .when(memberService).deleteMember(MEMBER_ID);

        mockMvc.perform(delete("/member/{id}", MEMBER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"));
    }

    @Test
    void deleteMember_whenMemberHasActiveLoans_returnsConflict() throws Exception {
        doThrow(new IllegalStateException("Member has active loans"))
                .when(memberService).deleteMember(MEMBER_ID);

        mockMvc.perform(delete("/member/{id}", MEMBER_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Unable to proceed operation"))
                .andExpect(jsonPath("$.detail").value("Member has active loans"));
    }
}
