package com.wikt0r2115.library.controller.dto;

import jakarta.validation.constraints.NotNull;

public record CreateLoanRequest(
        @NotNull
        Long bookId,
        @NotNull
        Long memberId
) {
}
