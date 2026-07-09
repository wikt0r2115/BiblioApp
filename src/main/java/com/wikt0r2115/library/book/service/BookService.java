package com.wikt0r2115.library.book.service;

import com.wikt0r2115.library.book.entity.Book;
import com.wikt0r2115.library.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(String isbn, String title, int publicationYear, String author, String category){
        Book book = new Book(isbn, title, publicationYear, author, category);
        return bookRepository.save(book);
    }

    public Book findById(Long id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found "+id));
    }

    public List<Book> findAll(){
        return bookRepository.findAll();
    }

    public Book updateDetails(Long id, String title, int publicationYear, String author, String category){
        Book book = findById(id);
        book.updateDetails(title, publicationYear, author, category);
        return bookRepository.save(book);
    }

    public Book changeIsbn(Long id, String isbn){
        Book book = findById(id);
        book.changeIsbn(isbn);
        return bookRepository.save(book);
    }

    public Book markBorrowed(Long id){
        Book book = findById(id);
        book.markBorrowed();
        return bookRepository.save(book);
    }

    public Book markReturned(Long id){
        Book book = findById(id);
        book.markReturned();
        return bookRepository.save(book);
    }

    public Book deleteBook(Long id){
        Book book = findById(id);
        bookRepository.delete(book);
        return book;
    }
}
