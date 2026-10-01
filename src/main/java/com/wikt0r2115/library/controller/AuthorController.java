package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.controller.dto.AuthorResponse;
import com.wikt0r2115.library.controller.dto.CreateAuthorRequest;
import com.wikt0r2115.library.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/author")
public class AuthorController {
    private final AuthorService authorService;

    AuthorController(AuthorService authorService) { this.authorService = authorService; }

    @GetMapping
    public List<AuthorResponse> findAuthors(){
        return authorService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createNewAuthor(@Valid @RequestBody CreateAuthorRequest request){
        return authorService.createAuthor(
                request.firstName(),
                request.lastName()
        );
    }
}
