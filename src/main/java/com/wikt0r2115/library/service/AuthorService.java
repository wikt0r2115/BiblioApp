package com.wikt0r2115.library.service;

import com.wikt0r2115.library.domain.Author;
import com.wikt0r2115.library.infrastructure.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository){ this.authorRepository = authorRepository; }

    public Author createAuthor(String firstName, String lastName){
        Author author = new Author(firstName, lastName);
        return authorRepository.save(author);
    }

    public List<Author> findAll(){
        return authorRepository.findAll();
    }
}
