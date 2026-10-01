package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.controller.dto.*;
import com.wikt0r2115.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/book")
public class BookController {
    private final BookService bookService;

    BookController(BookService bookService){
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public BookResponse findBookById(@PathVariable Long id){
        return bookService.findByIdWhereArchivedFalse(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createNewBook(@Valid @RequestBody CreateBookRequest request){
        return bookService.createBook(
                request.isbn(),
                request.title(),
                request.publicationYear(),
                request.authorId(),
                request.categoryIds());
    }

    @GetMapping
    public PageResponse<BookResponse> findBooks(@Valid @ModelAttribute BookFilterRequest filter, Pageable pageable){
        return bookService.findAll(
                filter.authorId(),
                filter.title(),
                filter.available(),
                pageable
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id){
        bookService.deleteBook(id);
    }

    @PutMapping("/{id}")
    public BookResponse updateDetails(@PathVariable Long id, @Valid @RequestBody UpdateBookDetailsRequest request){
        return bookService.updateDetails(
                id,
                request.title(),
                request.publicationYear(),
                request.authorId(),
                request.categoryIds());
    }

    @PutMapping("/{id}/isbn")
    public BookResponse changeIsbn(@PathVariable Long id, @Valid @RequestBody ChangeBookIsbnRequest request){
        return bookService.changeIsbn(id,request.isbn());
    }

}
