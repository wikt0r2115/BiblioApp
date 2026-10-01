package com.wikt0r2115.library.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Author {
    @Id
    @GeneratedValue
    private Long authorId;

    @NotBlank(message = "First name must not be blank")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @Column(nullable = false)
    private String lastName;

    protected Author(){}

    public Author(String firstName, String lastName){
        this.firstName = validateString(firstName);
        this.lastName = validateString(lastName);
    }

    private String validateString(String string){
        if(string == null || string.isBlank())
            throw new IllegalArgumentException("first or last name must not be blank");
        if(string.strip().length() > 255)
            throw new IllegalArgumentException("First or last name must not exceed 255 characters");
        return string.strip();
    }

    public Long getAuthorId() { return authorId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
