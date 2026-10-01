package com.wikt0r2115.library.service;

import com.wikt0r2115.library.controller.dto.AuthorResponse;
import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.infrastructure.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository){ this.authorRepository = authorRepository; }

    public AuthorResponse createAuthor(String firstName, String lastName){
        Author author = new Author(firstName, lastName);
        return AuthorResponse.from(authorRepository.save(author));
    }

    public List<AuthorResponse> findAll(){
        return authorRepository.findAll().stream()
                .map(AuthorResponse::from)
                .toList();
    }
}
