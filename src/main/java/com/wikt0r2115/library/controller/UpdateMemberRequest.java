package com.wikt0r2115.library.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateMemberRequest (
        @NotBlank(message = "First name must not be blank")
        String firstName,
        @NotBlank(message = "Last name must not be blank")
        String lastName,
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        String email
){ }
