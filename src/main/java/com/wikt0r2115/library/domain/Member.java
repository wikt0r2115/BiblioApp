package com.wikt0r2115.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Member {
    @Id
    @GeneratedValue
    private Long memberId;

    @NotBlank(message = "first name must not be blank")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "last name must not be blank")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "email must not be blank")
    @Email(message = "invalid email")
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean archived = false;

    protected Member(){}

    public Member(String firstName, String lastName, String email){
        this.firstName = normalizeString(firstName);
        this.lastName = normalizeString(lastName);
        this.email = normalizeEmail(email);
    }

    public void updateDetails(String firstName, String lastName, String email){
        String newFirstName = normalizeString(firstName);
        String newLastName = normalizeString(lastName);
        String newEmail = normalizeEmail(email);
        this.firstName = newFirstName;
        this.lastName = newLastName;
        this.email = newEmail;
    }

    public void markArchived(){
        archived = true;
    }

    private String normalizeString(String string){
        if(string == null || string.isBlank())
            throw new IllegalArgumentException("Must not be blank");
        if(string.strip().length() > 255)
            throw new IllegalArgumentException("Must not exceed 255 characters");
        return string.strip();
    }

    private String normalizeEmail(String email){
        String normalizedEmail = normalizeString(email);
        String regex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
        if(!normalizedEmail.matches(regex))
            throw new IllegalArgumentException("Email must be valid");
        return normalizedEmail;
    }

    public Long getMemberId() { return memberId; }
    public String getFirstName(){ return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public boolean isArchived() {return archived;}
}
