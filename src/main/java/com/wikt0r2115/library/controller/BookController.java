package com.wikt0r2115.library.controller;

import com.wikt0r2115.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
        return BookResponse.from(bookService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createNewBook(@Valid @RequestBody CreateBookRequest request){
        return BookResponse.from(bookService.createBook(
                request.isbn(),
                request.title(),
                request.publicationYear(),
                request.author(),
                request.category()));
    }

    @GetMapping
    public PageResponse<BookResponse> findBooks(BookFilterRequest filter, Pageable pageable){
        Page<BookResponse> page = bookService.findAll(
                filter.author(),
                filter.title(),
                filter.available(),
                pageable
        ).map(BookResponse::from);
        return PageResponse.from(page);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id){
        bookService.deleteBook(id);
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

    @PutMapping("/{id}/isbn")
    public BookResponse changeIsbn(@PathVariable Long id, @Valid @RequestBody ChangeBookIsbnRequest request){
        return BookResponse.from(bookService.changeIsbn(id,request.isbn()));
    }

    @PutMapping("/{id}/borrow")
    public BookResponse markBorrowed(@PathVariable Long id){
        return BookResponse.from(bookService.markBorrowed(id));
    }

    @PutMapping("/{id}/return")
    public BookResponse markReturned(@PathVariable Long id){
        return BookResponse.from(bookService.markReturned(id));
    }
}
