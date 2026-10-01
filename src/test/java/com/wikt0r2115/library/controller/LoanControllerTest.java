package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.controller.dto.LoanResponse;
import com.wikt0r2115.library.domain.Loan;
import com.wikt0r2115.library.service.*;
import com.wikt0r2115.library.service.exception.BookAlreadyLoanedException;
import com.wikt0r2115.library.service.exception.BookNotFoundException;
import com.wikt0r2115.library.service.exception.LoanNotFoundException;
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

import java.util.List;

import static com.wikt0r2115.library.TestData.sampleActiveLoanWithIds;
import static com.wikt0r2115.library.TestData.sampleReturnedLoanWithIds;
import static com.wikt0r2115.library.TestData.MEMBER_ID;
import static com.wikt0r2115.library.TestData.LOAN_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoanControllerTest {
    @Mock
    private LoanService loanService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new LoanController(loanService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void borrowBook_whenRequestIsValid_returnsCreatedLoan() throws Exception {
        when(loanService.borrowBook(1L, 2L))
                .thenReturn(LoanResponse.from(activeLoan()));

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 1,
                                  "memberId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").value(10))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookTitle").value("Atomic Habits"))
                .andExpect(jsonPath("$.memberFirstName").value("Jan"))
                .andExpect(jsonPath("$.memberLastName").value("Kowalski"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void borrowBook_whenRequestIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid"));
        verifyNoInteractions(loanService);
    }

    @Test
    void borrowBook_whenBookDoesNotExist_returnsNotFound() throws Exception {
        when(loanService.borrowBook(1L, 2L))
                .thenThrow(new BookNotFoundException(1L));

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 1,
                                  "memberId": 2
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail").value("Book does not exist"));
    }


    @Test
    void findLoanById_whenLoanExists_returnsLoan() throws Exception {
        when(loanService.findById(10L))
                .thenReturn(LoanResponse.from(activeLoan()));

        mockMvc.perform(get("/loans/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(10))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void returnBook_whenLoanExists_returnsReturnedLoan() throws Exception {
        when(loanService.returnBook(10L))
                .thenReturn(LoanResponse.from(returnedLoan()));

        mockMvc.perform(post("/loans/10/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(10))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void returnBook_whenLoanDoesNotExist_returnsNotFound() throws Exception {
        when(loanService.returnBook(10L))
                .thenThrow(new LoanNotFoundException(10L));

        mockMvc.perform(post("/loans/10/return"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Loan not found"))
                .andExpect(jsonPath("$.detail").value("Loan does not exist"));
    }

    @Test
    void getActiveLoans_returnsLoans() throws Exception {
        when(loanService.findActiveLoansByMember(2L))
                .thenReturn(List.of(LoanResponse.from(activeLoan())));

        mockMvc.perform(get("/members/2/loans/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].loanId").value(10))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getHistoryLoans_returnsLoans() throws Exception {
        when(loanService.findReturnedLoansByMember(2L))
                .thenReturn(List.of(LoanResponse.from(returnedLoan())));

        mockMvc.perform(get("/members/2/loans/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].loanId").value(10))
                .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void borrowBook_whenBookAlreadyLoaned_returnsConflict() throws Exception{
        when(loanService.borrowBook(1L, 2L))
                .thenThrow(new BookAlreadyLoanedException(1L));

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                               {
                                "bookId": 1,
                                "memberId": 2
                               }
                               """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Book is loaned"))
                .andExpect(jsonPath("$.detail").value("Book with id 1 is already loaned"));
    }

    @Test
    void borrowBook_whenMemberDoesNotExists_returnNotFound() throws Exception{
        when(loanService.borrowBook(1L, 3L))
                .thenThrow(new MemberNotFoundException(3L));

        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "bookId": 1,
                            "memberId": 3
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"))
                .andExpect(jsonPath("$.detail").value("Member does not exist"));
    }

    @Test
    void findLoanById_whenLoanDoesNotExist_returnsNotFound() throws Exception {
        when(loanService.findById(LOAN_ID)).thenThrow(new LoanNotFoundException(LOAN_ID));

        mockMvc.perform(get("/loans/{loanId}", LOAN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Loan not found"));
    }

    @Test
    void returnBook_whenAlreadyReturned_returnsConflict() throws Exception {
        when(loanService.returnBook(LOAN_ID))
                .thenThrow(new IllegalStateException("book is already returned"));

        mockMvc.perform(post("/loans/{loanId}/return", LOAN_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Unable to proceed operation"));
    }

    @Test
    void getActiveLoans_whenMemberDoesNotExist_returnsNotFound() throws Exception {
        when(loanService.findActiveLoansByMember(MEMBER_ID))
                .thenThrow(new MemberNotFoundException(MEMBER_ID));

        mockMvc.perform(get("/members/{memberId}/loans/active", MEMBER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"));
    }

    @Test
    void getHistoryLoans_whenMemberDoesNotExist_returnsNotFound() throws Exception {
        when(loanService.findReturnedLoansByMember(MEMBER_ID))
                .thenThrow(new MemberNotFoundException(MEMBER_ID));

        mockMvc.perform(get("/members/{memberId}/loans/history", MEMBER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"));
    }

    @Test
    void getActiveLoans_whenNoLoansExist_returnsEmptyList() throws Exception {
        when(loanService.findActiveLoansByMember(MEMBER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/members/{memberId}/loans/active", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getHistoryLoans_whenNoLoansExist_returnsEmptyList() throws Exception {
        when(loanService.findReturnedLoansByMember(MEMBER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/members/{memberId}/loans/history", MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    private Loan activeLoan() {
        return sampleActiveLoanWithIds();
    }

    private Loan returnedLoan() {
        return sampleReturnedLoanWithIds();
    }
}
