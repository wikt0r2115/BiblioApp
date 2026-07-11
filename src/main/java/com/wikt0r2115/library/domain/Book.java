package com.wikt0r2115.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.Year;

@Entity
public class Book {
    @Id
    @GeneratedValue
    private Long bookId;

    @Column(nullable = false, unique = true)
    //@ISBN
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column
    private int publicationYear;

    @Column
    private boolean available;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String category;

    protected Book(){}

    public Book(String isbn, String title, int publicationYear, String author, String category){
        this.isbn = normalizeIsbn(isbn);
        this.title = normalizeTitle(title);
        this.publicationYear = normalizePublicationYear(publicationYear);
        this.available = true;
        this.author = normalizeAuthor(author);
        this.category = normalizeCategory(category);
    }

    private String normalizeIsbn(String isbn){
        if(isbn == null || isbn.isBlank())
            throw new IllegalArgumentException("ISBN must not be blank");
        isbn = isbn.replace("-","");
        isbn = isbn.replaceAll("\\s+","");
        isbn = isbn.replace("x","X");
        if(!validateIsbn10(isbn) && !validateIsbn13(isbn))
            throw new IllegalArgumentException("ISBN is invalid");
        return isbn;
    }

    private boolean validateIsbn10(String isbn){
        if(isbn.length() != 10)
            return false;

        if(isbn.charAt(9) != 'X' && !Character.isDigit(isbn.charAt(9)))
            return false;

        for(int i = 0; i < 9; i++){
            if(!Character.isDigit(isbn.charAt(i)))
                return false;
        }
        int sum = 0;
        int weight = 10;
        for(int i = 0; i < 10; i++){
            if(isbn.charAt(i) == 'X') {
                sum += 10;
                continue;
            }
            sum += Character.getNumericValue(isbn.charAt(i)) * weight;
            weight--;
        }
        return sum % 11 == 0;
    }

    private boolean validateIsbn13(String isbn){
        if(isbn.length() != 13)
            return false;
        for(int i = 0; i < 13; i++){
            if(!Character.isDigit(isbn.charAt(i)))
                return false;
        }
        if(!isbn.startsWith("978") && !isbn.startsWith("979"))
            return false;
        int sum = 0;
        int weight;
        for(int i = 0; i < 12; i++){
            weight = (i%2==0)? 1 : 3;
            sum += Character.getNumericValue(isbn.charAt(i)) * weight;
        }

        return (10 - (sum % 10))%10 == Character.getNumericValue(isbn.charAt(12));
    }

    private String normalizeTitle(String title){
        if(title == null || title.isBlank())
            throw new IllegalArgumentException("Title must not be blank");

        return title.strip();
    }

    private int normalizePublicationYear(int publicationYear){
        int lowestYear = 1450;
        int currentYear = Year.now().getValue();
        if(publicationYear < lowestYear || publicationYear > currentYear)
            throw new IllegalArgumentException("Year must be in range from "+ lowestYear +" to " + currentYear);

        return publicationYear;
    }

    private String normalizeAuthor(String author){
        if(author == null || author.isBlank())
            throw new IllegalArgumentException("Author must not be blank");
        return author.strip();
    }

    private String normalizeCategory(String category){
        if(category == null || category.isBlank())
            throw new IllegalArgumentException("Category must not be blank");
        return category.strip();
    }

    public void updateDetails(String title, int publicationYear, String author, String category) {
        String newTitle = normalizeTitle(title);
        int newPublicationYear = normalizePublicationYear(publicationYear);
        String newAuthor = normalizeAuthor(author);
        String newCategory = normalizeCategory(category);
        this.title = newTitle;
        this.publicationYear = newPublicationYear;
        this.author = newAuthor;
        this.category = newCategory;
    }

    public void changeIsbn(String isbn) {
        this.isbn = normalizeIsbn(isbn);
    }

    public void markBorrowed() {
        if (!available) {
            throw new IllegalStateException("Book is not available");
        }
        this.available = false;
    }

    public void markReturned() {
        if (available) {
            throw new IllegalStateException("Book is already available");
        }
        this.available = true;
    }


    public Long getBookId() {return bookId;}
    public String getTitle(){return title;}
    public boolean isAvailable(){return available;}
    public String getAuthor(){return author;}
    public String getCategory(){return category;}
    public String getIsbn(){return isbn;}
    public int getPublicationYear(){return publicationYear;}
}
