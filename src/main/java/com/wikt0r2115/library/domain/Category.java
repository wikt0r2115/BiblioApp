package com.wikt0r2115.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Category {
    @Id
    @GeneratedValue
    private Long categoryId;

    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false, unique = true)
    private String name;

    protected Category(){}

    public Category(String name){
        this.name = validateName(name);
    }

    private String validateName(String name){
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("Name must not be blank");
        if(name.strip().length() > 255)
            throw new IllegalArgumentException("Name must not exceed 255 characters");
        return name.strip();
    }

    public Long getCategoryId() { return categoryId; }
    public String getName() { return name; }
}
