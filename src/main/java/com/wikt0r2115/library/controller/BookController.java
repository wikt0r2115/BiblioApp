package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return BookResponse.from(bookService.findById(id));
    }

    @PostMapping
    public BookResponse createNewBook(@Valid @RequestBody CreateBookRequest request){
        return BookResponse.from(bookService.createBook(
                request.isbn(),
                request.title(),
                request.publicationYear(),
                request.author(),
                request.category()));
    }

    @GetMapping
    public Page<BookResponse> findBooks(BookFilterRequest filter, Pageable pageable){
        return bookService.findAll(
                        filter.author(),
                        filter.title(),
                        filter.available(),
                        pageable)
                .map(BookResponse::from);
    }

    @DeleteMapping("/{id}")
    public BookResponse deleteBook(@PathVariable Long id){
        return BookResponse.from(bookService.deleteBook(id));
    }

    @PutMapping("/{id}")
    public BookResponse updateDetails(@PathVariable Long id, @Valid @RequestBody UpdateBookDetailsRequest request){
        return BookResponse.from(bookService.updateDetails(
                id,
                request.title(),
                request.publicationYear(),
                request.author(),
                request.category()));
    }

    @PutMapping("{id}/{isbn}")
    public BookResponse changeIsbn(@PathVariable Long id, @PathVariable String isbn){
        return BookResponse.from(bookService.changeIsbn(id,isbn));
    }
}
