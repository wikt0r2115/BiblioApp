package com.wikt0r2115.library.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Year;
import java.util.Objects;
import java.util.Set;

@Entity
public class Book {
    @Id
    @GeneratedValue
    private Long bookId;

    @NotBlank(message = "isbn must not be blank")
    @Column(nullable = false, unique = true)
    //@ISBN
    private String isbn;

    @NotBlank(message = "title must not be blank")
    @Column(nullable = false)
    private String title;

    @Min(value = 1450, message = "Year must be minimum 1450")
    @Column
    private int publicationYear;

    @Column
    private boolean available;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToMany
    @JoinTable(name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
            )
    private Set<Category> categories;

    @Column(nullable = false)
    private boolean archived = false;

    protected Book(){}

    public Book(String isbn, String title, int publicationYear, Author author, Set<Category> categories){
        this.isbn = normalizeIsbn(isbn);
        this.title = normalizeTitle(title);
        this.publicationYear = normalizePublicationYear(publicationYear);
        this.available = true;
        this.author = normalizeAuthor(author);
        this.categories = normalizeCategories(categories);
    }

    private String normalizeIsbn(String isbn){
        if(isbn == null || isbn.isBlank())
            throw new IllegalArgumentException("ISBN must not be blank");
        isbn = isbn.replace("-","");
        isbn = isbn.replaceAll("\\s+","");
        isbn = isbn.replace("x","X");
        if(isbn.length() > 255)
            throw new IllegalArgumentException("ISBN must not exceed 255 characters");
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
        if(title.strip().length() > 255)
            throw new IllegalArgumentException("Title must not exceed 255 characters");

        return title.strip();
    }

    private int normalizePublicationYear(int publicationYear){
        int lowestYear = 1450;
        int currentYear = Year.now().getValue();
        if(publicationYear < lowestYear || publicationYear > currentYear)
            throw new IllegalArgumentException("Year must be in range from "+ lowestYear +" to " + currentYear);

        return publicationYear;
    }

    private Author normalizeAuthor(Author author){
        if(author == null)
            throw new IllegalArgumentException("Author must not be null");
        return author;
    }

    private Set<Category> normalizeCategories(Set<Category> categories){
        if(categories == null || categories.isEmpty())
            throw new IllegalArgumentException("Categories must not be empty");
        if(categories.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Categories must not contain null");
        return categories;
    }

    public void updateDetails(String title, int publicationYear, Author author, Set<Category> categories) {
        String newTitle = normalizeTitle(title);
        int newPublicationYear = normalizePublicationYear(publicationYear);
        Author newAuthor = normalizeAuthor(author);
        Set<Category> newCategories = normalizeCategories(categories);
        this.title = newTitle;
        this.publicationYear = newPublicationYear;
        this.author = newAuthor;
        this.categories = newCategories;
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

    public void markArchived(){
        this.archived = true;
    }

    public Long getBookId() {return bookId;}
    public String getTitle(){return title;}
    public boolean isAvailable(){return available;}
    public Author getAuthor(){return author;}
    public Set<Category> getCategories(){return categories;}
    public String getIsbn(){return isbn;}
    public int getPublicationYear(){return publicationYear;}
    public boolean isArchived() {return archived;}
}
